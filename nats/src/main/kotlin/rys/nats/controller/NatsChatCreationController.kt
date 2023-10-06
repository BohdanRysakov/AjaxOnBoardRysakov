package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsMongoChatParser.deserializeChat
import rys.nats.utils.NatsMongoChatParser.serializeChat
import rys.nats.utils.NatsMongoChatParser.serializeMongoChats
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService

@Service
class NatsChatCreationController(private val natsConnection: Connection,
                                 private val chatService: ChatService
) {
    @PostConstruct
    fun init() {
        natsConnection.createDispatcher().subscribe("chat.create") { message ->

            val newChat = chatService.createChat(deserializeChat(message.data))

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, serializeChat(newChat))
            }
        }
    }
}
