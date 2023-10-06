package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsMongoChatParser.serializeMongoChats
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService

@Service
class NatsChatFindAllController(private val natsConnection: Connection,
                                private val chatService: ChatService
) {

    @PostConstruct
    fun init() {
        val dispatcher : Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.findAll") { message ->

            val allChats = chatService.findAllChats()

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, serializeMongoChats(allChats))
            }
        }
    }
}
