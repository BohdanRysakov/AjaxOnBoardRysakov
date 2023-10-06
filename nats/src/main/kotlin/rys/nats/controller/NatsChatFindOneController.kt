package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import rys.nats.utils.NatsMongoChatParser.deserializeFindChatRequest
import rys.nats.utils.NatsMongoChatParser.serializeChat
import rys.rest.exceptions.ChatNotFoundException
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService


@Component
class NatsChatFindOneController(
    private val natsConnection: Connection,
    private val chatService: ChatService
) {
    @PostConstruct
    fun init() {
        val dispatcher: Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.findOne") { message ->

            val idToFind = deserializeFindChatRequest(message.data)

            val chat = chatService.findChatById(ObjectId(idToFind))

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, serializeChat(chat))
            }
        }
    }
}
