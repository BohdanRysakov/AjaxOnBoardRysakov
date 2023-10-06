package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import rys.nats.utils.NatsMongoChatParser
import rys.nats.utils.NatsMongoChatParser.serializeChat
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService


@Component
class NatsChatUpdateController(private val natsConnection: Connection,
                               private val chatService: ChatService
) {
    @PostConstruct
    fun init() {
        val dispatcher : Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.update") { message ->

            val idToUpdate = NatsMongoChatParser.deserializeUpdateRequest(message.data).first
            val updatedChat = NatsMongoChatParser.deserializeUpdateRequest(message.data).second

           val newChat = chatService.updateChat(ObjectId(idToUpdate),updatedChat)

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, serializeChat(newChat))
            }
        }
    }
}
