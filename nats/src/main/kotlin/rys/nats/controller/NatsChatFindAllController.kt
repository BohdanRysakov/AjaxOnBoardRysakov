package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat
import rys.rest.service.ChatService

@Service
class NatsChatFindAllController(
    private val natsConnection: Connection,
    private val chatService: ChatService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        val dispatcher: Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.findAll") { message ->
            try {
                val chats: List<MongoChat> = chatService.findAllChats()

                val response = Mongochat.ChatFindAllResponse.newBuilder().apply {
                    successBuilder.apply {
                        chats.forEach {
                            Mongochat.Chat.newBuilder().apply {
                                id = it.id.toString()
                                name = it.name
                                it.users.forEach {
                                    this.addUsers(it.toString())
                                }

                            }
                        }
                    }
                }.build()

                message.replyTo?.let { replaySubject ->
                    natsConnection.publish(
                        replaySubject, NatsValidMongoChatParser.serializeFindChatsResponse(response)
                    )
                }

            } catch (e: Exception) {
                logger.error("Error while finding all chats: ${e.message}", e)

                val response = Mongochat.ChatFindAllResponse.newBuilder().apply {
                    failureBuilder.apply {
                        this.message = e.message
                        this.internalErrorBuilder
                    }
                }.build()

                message.replyTo?.let { replaySubject ->
                    natsConnection.publish(
                        replaySubject, NatsValidMongoChatParser.serializeFindChatsResponse(response)
                    )
                }

            }
        }
    }
}
