package rys.ajaxpetproject.nats

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import rys.ajaxpetproject.nats.exception.InternalException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.commonmodels.chat.proto.Chat
import rys.ajaxpetproject.nats.config.NatsControllerConfigurerPostProcessor
import rys.ajaxpetproject.nats.controller.impl.NatsChatCreationController
import rys.ajaxpetproject.nats.controller.impl.NatsChatDeleteController
import rys.ajaxpetproject.nats.controller.impl.NatsChatFindAllController
import rys.ajaxpetproject.nats.controller.impl.NatsChatUpdateController
import rys.ajaxpetproject.nats.controller.impl.NatsChatFindOneController
import rys.ajaxpetproject.repository.ChatRepository
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
import rys.ajaxpetproject.subjects.ChatSubjectsV1
import java.time.Duration

@SpringBootTest(classes = [NatsTestConfiguration::class])
@ContextConfiguration(
    classes = [
        ChatRepository::class, ChatServiceImpl::class,
        NatsChatCreationController::class,
        NatsChatDeleteController::class,
        NatsChatFindAllController::class,
        NatsChatFindOneController::class,
        NatsChatUpdateController::class,
        NatsControllerConfigurerPostProcessor::class
        ]
)
@ActiveProfiles("testing")
class NatsControllersTests {
    @SpyBean
    private lateinit var chatService: ChatService

    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @BeforeEach
    fun clearTestDB() {
        chatRepository.deleteAll()
    }

    @Test
    fun `Nats chat creation success scenario`() {
        val initialChat = MongoChat(
            id = ObjectId(),
            name = "test chat success",
            users = listOf(ObjectId(), ObjectId())
        )
        val request = ChatCreateRequest.newBuilder()
            .apply {
                this.chat = Chat.newBuilder()
                    .apply {
                        this.id = initialChat.id.toString()
                        this.name = initialChat.name
                        initialChat.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()
        val response = ChatCreateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.CREATE,
                request.toByteArray(),
                Duration.ofSeconds(3)
            ).data
        )

        assert(response.hasSuccess())

        val successfulChat: MongoChat = response.success.result.let {
            MongoChat(
                id = ObjectId(it.id),
                name = it.name,
                users = it.usersList.map { ObjectId(it) }
            )
        }
        val chatFromDB: MongoChat = chatRepository.findById(initialChat.id!!).get()

        assert(chatFromDB == successfulChat)
    }

    @Test
    fun `Nats chat creation failure scenario`() {
        val initialChat = MongoChat(
            id = ObjectId(),
            name = "test chat failure",
            users = listOf(ObjectId(), ObjectId())
        )
        whenever(chatService.createChat(initialChat)).thenThrow(InternalException("Test exception"))
        val request = ChatCreateRequest.newBuilder()
            .apply {
                this.chat = Chat.newBuilder()
                    .apply {
                        this.id = initialChat.id.toString()
                        this.name = initialChat.name
                        initialChat.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()
        val response = ChatCreateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.CREATE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        assert(response.hasFailure())

        val failureMessage = ChatCreateResponse.newBuilder().apply {
            failureBuilder.internalErrorBuilder
            failureBuilder.message = "Test exception"
        }.build()

        assert(response == failureMessage)
    }

    @Test
    fun `Nats chat delete success scenario`() {
        val chatToDelete = MongoChat(
            id = ObjectId(),
            name = "test chat success",
            users = listOf(ObjectId(), ObjectId())
        )
        chatRepository.save(chatToDelete)

        val request = ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = chatToDelete.id.toString()
            }.build()
        val response = ChatDeleteResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.DELETE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )

        assert(response.hasSuccess())
        assert(response.success.result)
    }

    @Test
    fun `Nats chat delete failure scenario`() {
        val request = ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = ObjectId().toString()
            }.build()
        val response = ChatDeleteResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.DELETE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasFailure())
        assert(response.failure.message == "Chat not found")
    }

    @Test
    fun `Nats chat find all success scenario`() {
        val listOfUsers: MutableList<MongoChat> = mutableListOf()
        for (i in 1..10) {
            val chat = MongoChat(
                id = ObjectId(),
                name = "test chat success $i",
                users = listOf(ObjectId(), ObjectId())
            )
            chatRepository.save(chat)
            listOfUsers.add(chatRepository.findChatById(chat.id!!)!!)
        }
        val response = ChatFindAllResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ALL,
                ByteArray(0),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasSuccess())

        val listFromResponse = response.success.resultList.map {
            MongoChat(
                id = ObjectId(it.id),
                name = it.name,
                users = it.usersList.map { ObjectId(it) }
            )
        }
        assert(listFromResponse == listOfUsers)
    }

    @Test
    fun `Nats chat find all failure scenario`() {
        whenever(chatService.findAllChats()).thenThrow(InternalException("Test exception"))
        val response = ChatFindAllResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ALL,
                ByteArray(0),
                Duration.ofSeconds(10)
            ).data
        )

        assert(response.hasFailure())
        assert(response.failure.message == "Test exception")
        assert(response.failure.internalError.isInitialized)
    }

    @Test
    fun `Nats chat find one success scenario`() {
        val chatToFind = MongoChat(
            id = ObjectId(),
            name = "test chat success",
            users = listOf(ObjectId(), ObjectId())
        )
        chatRepository.save(chatToFind)
        val request = ChatFindOneRequest.newBuilder()
            .apply {
                this.id = chatToFind.id.toString()
            }.build()
        val response = ChatFindOneResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ONE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasSuccess())

        val chatFromResponse = response.success.result.let {
            MongoChat(
                id = ObjectId(it.id),
                name = it.name,
                users = it.usersList.map { ObjectId(it) }
            )
        }
        assert(chatFromResponse == chatToFind)
    }

    @Test
    fun `Nats chat find one failure scenario`() {
        val chatToFind = MongoChat(
            id = ObjectId(),
            name = "test chat success",
            users = listOf(ObjectId(), ObjectId())
        )
        chatRepository.save(chatToFind)

        val idToFind = chatToFind.id!!

        val request = ChatFindOneRequest.newBuilder()
            .apply {
                this.id = idToFind.toString()
            }.build()
        whenever(chatService.findChatById(idToFind)).thenThrow(InternalException("Test exception"))

        val response = ChatFindOneResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.FIND_ONE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasFailure())
        assert(response.failure.message == "Test exception")
        assert(response.failure.internalError.isInitialized)
    }

    @Test
    fun `Nats chat update success scenario`() {
        val chatToUpdate = MongoChat(
            id = ObjectId(),
            name = "test chat success UNCHANGED",
            users = listOf(ObjectId(), ObjectId())
        )
        val chatUpdatedVersion = MongoChat(
            id = ObjectId(),
            name = "test chat success",
            users = listOf(ObjectId(), ObjectId())
        )
        chatRepository.save(chatToUpdate)

        val idOfChatToUpdate = chatToUpdate.id!!

        val request = ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = idOfChatToUpdate.toString()
                this.chat = Chat.newBuilder()
                    .apply {
                        this.id = chatUpdatedVersion.id.toString()
                        this.name = chatUpdatedVersion.name
                        chatUpdatedVersion.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()
        val response = ChatUpdateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.UPDATE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasSuccess())

        val chatFromResponse = response.success.result.let {
            MongoChat(
                id = ObjectId(it.id),
                name = it.name,
                users = it.usersList.map { ObjectId(it) }
            )
        }
        assert(chatFromResponse == chatUpdatedVersion.copy(id = chatToUpdate.id))
    }

    @Test
    fun `Nats chat update failure scenario`() {
        val chatToUpdate = MongoChat(
            id = ObjectId(),
            name = "test chat success UNCHANGED",
            users = listOf(ObjectId(), ObjectId())
        )
        val chatUpdatedVersion = MongoChat(
            id = ObjectId(),
            name = "test chat success",
            users = listOf(ObjectId(), ObjectId())
        )
        chatRepository.save(chatToUpdate)

        val idOfChatToUpdate = chatUpdatedVersion.id!!

        whenever(
            chatService.updateChat(
                idOfChatToUpdate,
                chatUpdatedVersion
            )
        ).thenThrow(InternalException("Test Exception"))

        val request = ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = idOfChatToUpdate.toString()
                this.chat = Chat.newBuilder()
                    .apply {
                        this.id = chatUpdatedVersion.id.toString()
                        this.name = chatUpdatedVersion.name
                        chatUpdatedVersion.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()
        val response = ChatUpdateResponse.parseFrom(
            connection.request(
                ChatSubjectsV1.ChatRequest.UPDATE,
                request.toByteArray(),
                Duration.ofSeconds(10)
            ).data
        )
        assert(response.hasFailure())
        assert(response.failure.message == "Test Exception")
        assert(response.failure.internalError.isInitialized)
    }
}
