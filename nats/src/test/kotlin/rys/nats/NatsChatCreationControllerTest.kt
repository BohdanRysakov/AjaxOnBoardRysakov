package rys.nats

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
import rys.nats.controller.NatsChatCreationController
import rys.nats.controller.NatsChatDeleteController
import rys.nats.controller.NatsChatFindAllController
import rys.nats.controller.NatsChatUpdateController
import rys.nats.controller.NatsChatFindOneController
import rys.nats.exception.InternalException
import rys.nats.protostest.Mongochat
import rys.nats.protostest.Mongochat.ChatCreateResponse
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService
import rys.rest.service.impl.ChatServiceImpl
import java.time.Duration

@SpringBootTest(classes = [NatsTestConfiguration::class])
@ContextConfiguration(
    classes = [
        ChatRepository::class, ChatServiceImpl::class,
        NatsChatCreationController::class,
        NatsChatDeleteController::class,
        NatsChatFindAllController::class,
        NatsChatFindOneController::class,
        NatsChatUpdateController::class]
)
@ActiveProfiles("testing")
class NatsChatCreationControllerTest {

    @SpyBean
    private lateinit var chatService: ChatService

    @Autowired
    private lateinit var natsConnection: Connection

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

        val request = Mongochat.ChatCreateRequest.newBuilder()
            .apply {
                this.chat = Mongochat.Chat.newBuilder()
                    .apply {
                        this.id = initialChat.id.toString()
                        this.name = initialChat.name
                        initialChat.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()

        val response = NatsValidMongoChatParser.deserializeCreateChatResponse(
            natsConnection.request(
                "chat.create",
                NatsValidMongoChatParser.serializeCreateChatRequest(request),
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

        val request = Mongochat.ChatCreateRequest.newBuilder()
            .apply {
                this.chat = Mongochat.Chat.newBuilder()
                    .apply {
                        this.id = initialChat.id.toString()
                        this.name = initialChat.name
                        initialChat.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()

        val response = NatsValidMongoChatParser.deserializeCreateChatResponse(
            natsConnection.request(
                "chat.create",
                NatsValidMongoChatParser.serializeCreateChatRequest(request),
                Duration.ofSeconds(3)
            )
                .data
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

        val request = Mongochat.ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = chatToDelete.id.toString()
            }.build()

        val response = NatsValidMongoChatParser.deserializeDeleteChatResponse(
            natsConnection.request(
                "chat.delete",
                NatsValidMongoChatParser.serializeDeleteChatRequest(request),
                Duration.ofSeconds(10)
            )
                .data
        )

        assert(response.hasSuccess())

        assert(response.success.result)
    }

    @Test
    fun `Nats chat delete failure scenario`() {
        val request = Mongochat.ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = ObjectId().toString()
            }.build()

        val response = NatsValidMongoChatParser.deserializeDeleteChatResponse(
            natsConnection.request(
                "chat.delete",
                NatsValidMongoChatParser.serializeDeleteChatRequest(request),
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

        val response = NatsValidMongoChatParser.deserializeFindChatsResponse(
            natsConnection.request(
                "chat.findAll",
                NatsValidMongoChatParser.serializeFindChatsRequest(),
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

        val response = NatsValidMongoChatParser.deserializeFindChatsResponse(
            natsConnection.request(
                "chat.findAll",
                NatsValidMongoChatParser.serializeFindChatsRequest(),
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

        val request = Mongochat.ChatFindOneRequest.newBuilder()
            .apply {
                this.id = chatToFind.id.toString()
            }.build()

        val response = NatsValidMongoChatParser.deserializeFindChatResponse(
            natsConnection.request(
                "chat.findOne",
                NatsValidMongoChatParser.serializeFindChatRequest(request),
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

        val request = Mongochat.ChatFindOneRequest.newBuilder()
            .apply {
                this.id = idToFind.toString()
            }.build()

        whenever(chatService.findChatById(idToFind)).thenThrow(InternalException("Test exception"))

        val response = NatsValidMongoChatParser.deserializeDeleteChatResponse(
            natsConnection.request(
                "chat.findOne",
                NatsValidMongoChatParser.serializeFindChatRequest(request),
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

        val request = Mongochat.ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = idOfChatToUpdate.toString()
                this.chat = Mongochat.Chat.newBuilder()
                    .apply {
                        this.id = chatUpdatedVersion.id.toString()
                        this.name = chatUpdatedVersion.name
                        chatUpdatedVersion.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()

        val response = NatsValidMongoChatParser.deserializeUpdateResponse(
            natsConnection.request(
                "chat.update",
                NatsValidMongoChatParser.serializeUpdateRequest(request),
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

        val request = Mongochat.ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = idOfChatToUpdate.toString()
                this.chat = Mongochat.Chat.newBuilder()
                    .apply {
                        this.id = chatUpdatedVersion.id.toString()
                        this.name = chatUpdatedVersion.name
                        chatUpdatedVersion.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()

        val response = NatsValidMongoChatParser.deserializeUpdateResponse(
            natsConnection.request(
                "chat.update",
                NatsValidMongoChatParser.serializeUpdateRequest(request),
                Duration.ofSeconds(10)
            )
                .data
        )

        assert(response.hasFailure())

        assert(response.failure.message == "Test Exception")

        assert(response.failure.internalError.isInitialized)
    }
}
