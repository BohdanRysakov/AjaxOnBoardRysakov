package rys.nats

import io.nats.client.Connection
import io.nats.client.Message
import org.bson.types.ObjectId
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import rys.nats.config.NatsConfig
import rys.nats.controller.NatsChatCreationController
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService



@SpringBootTest(classes = arrayOf(NatsConfig::class,ChatRepository::class))
@ActiveProfiles("test")
class NatsChatCreationControllerTest {

    @MockBean
    lateinit var chatService: ChatService

    @Autowired
    private lateinit var natsConnection: Connection

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Test
    fun `Nats chat creation success scenario`(){
        val initialChat = MongoChat(
            id = ObjectId(),
            name = "test chat",
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

        whenever(chatService.createChat(any())).thenReturn(initialChat)


        val responseChat = NatsValidMongoChatParser.deserializeCreateChatResponse(
            natsConnection.request(
                "chat.create",
                NatsValidMongoChatParser.serializeCreateChatRequest(request)
            )
                .get()
                .data)

        assert(responseChat.hasSuccess())

        val successfulChat : MongoChat = responseChat.success.result.let {
            MongoChat(
                id = ObjectId(it.id),
                name = it.name,
                users = it.usersList.map { ObjectId(it) }
            )
        }

        val chatFromDB : MongoChat = chatRepository.findById(initialChat.id!!).get()

        assert(chatFromDB == successfulChat)
    }






}
