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
        natsConnection.createDispatcher().subscribe("chat.findAll") { message ->
            try {
                val chats: List<MongoChat> = chatService.findAllChats()

                val response = Mongochat.ChatFindAllResponse.newBuilder().apply {
                    successBuilder.apply {
                        chats.forEach { chat ->

                            this.addResult(
                                Mongochat.Chat.newBuilder().apply {
                                    id = chat.id.toString()
                                    name = chat.name
                                    chat.users.forEach {
                                        this.addUsers(it.toString())
                                    }
                                }.build()
                            )
                        }
                    }
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(it, NatsValidMongoChatParser.serializeFindChatsResponse(response))
                }
            } catch (e: Exception) {
                logger.error("Error while finding all chats: ${e.message}", e)

                val response = Mongochat.ChatFindAllResponse.newBuilder().apply {
                    failureBuilder.apply {
                        this.message = e.message
                        this.internalErrorBuilder
                    }
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(it, NatsValidMongoChatParser.serializeFindChatsResponse(response))
                }
            }
        }
    }
}
