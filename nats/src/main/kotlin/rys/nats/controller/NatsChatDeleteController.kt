package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import rys.nats.utils.NatsMongoChatParser
import rys.nats.utils.NatsMongoChatParser.deserializeDeleteRequest
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService

@Component
class NatsChatDeleteController(private val natsConnection: Connection,
                               private val chatService: ChatService
) {
    @PostConstruct
    fun init() {
         natsConnection.createDispatcher().subscribe("chat.delete") { message ->

            val idToDelete = deserializeDeleteRequest(message.data)

            chatService.deleteChat(ObjectId(idToDelete))

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, NatsMongoChatParser.serializeDeleteResponse(true))
            }
        }
    }
}
