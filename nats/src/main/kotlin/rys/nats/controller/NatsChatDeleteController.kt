package rys.nats.controller

import io.nats.client.Connection
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import rys.nats.natsservice.ProtobufService
import rys.nats.utils.NatsValidMongoChatParser.deserializeDeleteRequest
import rys.rest.service.ChatService

@Component
class NatsChatDeleteController(
    private val natsConnection: Connection,
    private val chatService: ChatService,
    private val protoService : ProtobufService
) {
    @PostConstruct
    fun init() {
        natsConnection.createDispatcher().subscribe("chat.delete") { message ->

            var idToDelete: String? = null
            try {
                idToDelete = deserializeDeleteRequest(message.data)
            } catch (e: Exception) {
                message.replyTo?.let { replySubject ->
                    natsConnection.publish(replySubject, protoService.serializeDeleteResponse(false))
                }
            }

            chatService.deleteChat(ObjectId(idToDelete))

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, protoService.serializeDeleteResponse(true))
            }
        }
    }
}
