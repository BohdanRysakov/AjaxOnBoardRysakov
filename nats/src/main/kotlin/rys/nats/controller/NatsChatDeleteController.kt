package rys.nats.controller

import io.nats.client.Connection
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rys.nats.protostest.Mongochat.ChatDeleteResponse
import rys.nats.utils.NatsValidMongoChatParser
import rys.nats.utils.NatsValidMongoChatParser.deserializeDeleteRequest
import rys.rest.service.ChatService

@Component
class NatsChatDeleteController(
    private val natsConnection: Connection,
    private val chatService: ChatService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        natsConnection.createDispatcher().subscribe("chat.delete") { message ->
            try {
                val requestBody: String = deserializeDeleteRequest(message.data).requestId

                chatService.deleteChat(ObjectId(requestBody))

                val response = ChatDeleteResponse.newBuilder().apply {
                    successBuilder.apply {
                        this.result = true
                    }
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(it, NatsValidMongoChatParser.serializeDeleteChatResponse(response))
                }


            } catch (e: Exception) {
                logger.error("Error while deleting chat: ${e.message}", e)

                val response = ChatDeleteResponse.newBuilder().setFailure(
                    ChatDeleteResponse.Failure.newBuilder().apply {
                        this.message = e.message
                        this.internalErrorBuilder
                    }.build()
                ).build()

                message.replyTo?.let {
                    natsConnection.publish(it, NatsValidMongoChatParser.serializeDeleteChatResponse(response))
                }
            }
        }
    }
}
