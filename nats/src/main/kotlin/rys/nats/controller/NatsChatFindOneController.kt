package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsValidMongoChatParser
import rys.nats.utils.NatsValidMongoChatParser.deserializeFindChatRequest
import rys.rest.service.ChatService

@Component
class NatsChatFindOneController(
    private val natsConnection: Connection,
    private val chatService: ChatService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        val dispatcher: Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.findOne") { message ->
            try {
                val requestBody : String  = deserializeFindChatRequest(message.data).id

                val chat  = chatService.findChatById(ObjectId(requestBody))

                val response = Mongochat.ChatFindOneResponse.newBuilder().apply {
                    successBuilder.apply {
                        this.result = Mongochat.Chat.newBuilder().apply {
                            id = chat!!.id.toString()
                            name = chat.name
                            chat.users.forEach {
                                this.addUsers(it.toString())
                            }
                        }.build()
                    }
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(it, NatsValidMongoChatParser.serializeFindChatResponse(response))
                }
            } catch (e: Exception) {
                logger.error("Error while finding chat: ${e.message}", e)

                val response = Mongochat.ChatFindOneResponse.newBuilder().apply {
                    failureBuilder.message = e.message
                    failureBuilder.internalErrorBuilder
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(it, NatsValidMongoChatParser.serializeFindChatResponse(response))
                }
            }
        }
    }
}
