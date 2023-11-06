package rys.ajaxpetproject.nats

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import rys.ajaxpetproject.nats.exception.InternalException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.configuration.SecurityConfiguration
import rys.ajaxpetproject.internalapi.ChatSubjectsV1
import rys.ajaxpetproject.kafka.MessageCreateEventProducer
import rys.ajaxpetproject.nats.config.NatsControllerConfigurerPostProcessor
import rys.ajaxpetproject.nats.controller.impl.chat.*
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.repository.impl.ChatRepositoryImpl
import rys.ajaxpetproject.repository.impl.MessageRepositoryImpl
import rys.ajaxpetproject.repository.impl.UserRepositoryImpl
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteRequest
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteResponse
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllResponse
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneRequest
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneResponse
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateRequest
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.impl.ChatServiceImpl
import rys.ajaxpetproject.service.impl.MessageServiceImpl
import rys.ajaxpetproject.service.impl.UserServiceImpl
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.utils.toProto
import java.time.Duration

@SpringBootTest(classes = [NatsTestConfiguration::class])
@ContextConfiguration(
    classes = [
        ChatRepositoryImpl::class, ChatServiceImpl::class,
        NatsChatCreationController::class,
        NatsChatDeleteController::class,
        NatsChatFindAllController::class, MessageRepositoryImpl::class, UserRepositoryImpl::class,
        NatsChatFindOneController::class, MessageServiceImpl::class, UserServiceImpl::class,
        NatsChatUpdateController::class, SecurityConfiguration::class,
        NatsControllerConfigurerPostProcessor::class
    ]
)
@ActiveProfiles("testing")
class NatsControllersIT {
    @SpyBean
    private lateinit var chatService: ChatService

    @MockBean
    private lateinit var kafkaSenderEvent : MessageCreateEventProducer

    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @BeforeEach
    fun clearTestDB() {
        chatRepository.deleteAll().block()
    }

    @Test
    fun `should return chat when request to create chat is published`() {
        //GIVEN
        val expectedChat = MongoChat(
            name = "test chat success",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        val request = ChatCreateRequest.newBuilder().apply {
            this.chat = expectedChat.toProto()
        }.build()

        whenever(kafkaSenderEvent.sendCreateEvent(any())).
        thenReturn(Unit.toMono())

        //WHEN
        val actualChat = ChatCreateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.CREATE,
                request.toByteArray(),
                Duration.ofSeconds(3)
            ).data
        ).success.result.toModel()

        //THEN
        Assertions.assertEquals(expectedChat, actualChat.copy(id = null))

        chatService.findChatById(actualChat.id!!).test()
            .expectSubscription()
            .assertNext { savedInDBChat ->
                Assertions.assertEquals(expectedChat.name, savedInDBChat.name)
                Assertions.assertEquals(expectedChat.users, savedInDBChat.users)
                Assertions.assertEquals(expectedChat.messages, savedInDBChat.messages)

            }
            .verifyComplete()
    }


    @Test
    fun `Nats chat creation failure scenario`() {
        //GIVEN
        val unexpectedChat = MongoChat(
            id = ObjectId().toString(),
            name = "test chat failure",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )

        val expectedExceptionMessage = "Test exception - ${System.nanoTime()}"

        val expectedException = InternalException(expectedExceptionMessage)

        whenever(chatService.save(unexpectedChat)).thenReturn(expectedException.toMono())


        val request = ChatCreateRequest.newBuilder()
            .apply {
                this.chat = unexpectedChat.toProto()
            }.build()

        //WHEN
        val actualResponse = ChatCreateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.CREATE,
                request.toByteArray(),
                Duration.ofSeconds(2)
            ).data
        )

        //THEN
        Assertions.assertTrue(actualResponse.hasFailure())
        Assertions.assertTrue(actualResponse.failure.hasInternalError())
        Assertions.assertEquals(expectedExceptionMessage, actualResponse.failure.message)
    }

    //
    @Test
    fun `Nats chat delete success scenario`() {
        //GIVEN
        val chatToDelete = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        chatRepository.save(chatToDelete).block()


        val request = ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = chatToDelete.id.toString()
            }.build()
        //WHEN
        val response = ChatDeleteResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.DELETE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        //THEN
        Assertions.assertTrue(response.hasSuccess())
        Assertions.assertTrue(response.success.result)
    }

    @Test
    fun `Nats chat delete failure scenario`() {
        //GIVEN
        val id = ObjectId().toString()

        val expectedMessage = "Chat with id $id not found"

        val request = ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = id
            }.build()

        //WHEN
        val response = ChatDeleteResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.DELETE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        //THEN
        Assertions.assertTrue(response.hasFailure())

        Assertions.assertEquals(expectedMessage, response.failure.message)
    }

    @Test
    fun `Nats chat find all success scenario`() {
        //GIVEN
        val listOfUsers: MutableList<MongoChat> = mutableListOf()

        for (i in 1..10) {
            val chat = MongoChat(
                id = ObjectId().toString(),
                name = "test chat success $i",
                users = listOf(ObjectId().toString(), ObjectId().toString())
            )
            chatRepository.save(chat).block()
            listOfUsers.add(chatRepository.findChatById(chat.id!!).block()!!)
        }

        //WHEN
        val response = ChatFindAllResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ALL,
                ByteArray(0),
                Duration.ofSeconds(3)
            ).data
        )

        //THEN
        Assertions.assertTrue(response.hasSuccess())

        val listFromResponse = response.success.resultList.map {
            it.toModel()
        }
        Assertions.assertEquals(listFromResponse, listOfUsers)
    }

    @Test
    fun `Nats chat find all failure scenario`() {
        //GIVEN
        whenever(chatService.findAll()).thenReturn(InternalException("Test exception").toFlux())

        //WHEN
        val response = ChatFindAllResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ALL,
                ByteArray(0),
                Duration.ofSeconds(10)
            ).data
        )

        //THEN
        assert(response.hasFailure())
        assert(response.failure.message == "Test exception")
        assert(response.failure.internalError.isInitialized)
    }

    @Test
    fun `Nats chat find one success scenario`() {
        //GIVEN
        val chatToFind = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        chatRepository.save(chatToFind).block()
        val request = ChatFindOneRequest.newBuilder()
            .apply {
                this.id = chatToFind.id.toString()
            }.build()

        //WHEN
        val response = ChatFindOneResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ONE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasSuccess())

        val chatFromResponse = response.success.result.toModel()
        assert(chatFromResponse == chatToFind)
    }

    @Test
    fun `Nats chat find one failure scenario`() {
        val chatToFind = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        chatRepository.save(chatToFind).block()

        val idToFind = chatToFind.id!!

        val request = ChatFindOneRequest.newBuilder()
            .apply {
                this.id = idToFind
            }.build()
        whenever(chatService.findChatById(idToFind)).thenReturn(InternalException("Test exception").toMono())

        //WHEN
        val response = ChatFindOneResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ONE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        //THEN
        assert(response.hasFailure())
        assert(response.failure.message == "Test exception")
        assert(response.failure.internalError.isInitialized)
    }

    @Test
    fun `Nats chat update success scenario`() {
        val chatToUpdate = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success UNCHANGED",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        val chatUpdatedVersion = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        chatRepository.save(chatToUpdate).block()

        val idOfChatToUpdate = chatToUpdate.id!!

        val request = ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = idOfChatToUpdate
                this.chat = chatUpdatedVersion.toProto()
            }.build()

        //WHEN
        val response = ChatUpdateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.UPDATE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        //THEN
        assert(response.hasSuccess())

        val chatFromResponse = response.success.result.toModel()
        assert(chatFromResponse == chatUpdatedVersion.copy(id = chatToUpdate.id))
    }

    @Test
    fun `Nats chat update failure scenario`() {
        val chatToUpdate = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success UNCHANGED",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        val chatUpdatedVersion = MongoChat(
            id = ObjectId().toString(),
            name = "test chat success",
            users = listOf(ObjectId().toString(), ObjectId().toString())
        )
        chatRepository.save(chatToUpdate).block()

        val idOfChatToUpdate = chatUpdatedVersion.id!!

        whenever(
            chatService.update(
                idOfChatToUpdate,
                chatUpdatedVersion
            )
        ).thenReturn(InternalException("Test Exception").toMono())

        val request = ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = idOfChatToUpdate
                this.chat = chatUpdatedVersion.toProto()
            }.build()

        //WHEN
        val response = ChatUpdateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.UPDATE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        //THEN
        assert(response.hasFailure())
        assert(response.failure.message == "Test Exception")
        assert(response.failure.internalError.isInitialized)
    }
}
