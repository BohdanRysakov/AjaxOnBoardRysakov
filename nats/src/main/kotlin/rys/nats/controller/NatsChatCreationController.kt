package rys.nats.controller

import io.nats.client.Connection
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.nats.protostest.Mongochat
import rys.nats.protostest.Mongochat.ChatCreateResponse
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat
import rys.rest.model.MongoUser
import rys.rest.service.ChatService

@Service
class NatsChatCreationController(
    private val natsConnection: Connection,
    private val chatService: ChatService
) {
    @PostConstruct
    fun init() {
        natsConnection.createDispatcher().subscribe("chat.create") { message ->

            val requestBody = NatsValidMongoChatParser
                .deserializeCreateChatRequest(message.data).chat

            var newChat: MongoChat?
            var response: ChatCreateResponse

            try {
                newChat = chatService.createChat(MongoChat(
                    id = ObjectId(requestBody.id),
                    name = requestBody.name,
                    users = requestBody.usersList.map { ObjectId(it) }
                ))

                response = ChatCreateResponse.newBuilder().apply {
                    successBuilder.apply {
                        this.result = Mongochat.Chat.newBuilder().apply {
                            id = newChat.id.toString()
                            name = newChat.name
                            newChat.users.forEach{
                                usersList.add(it.toString())
                            }
                        }.build()
                    }
                }.build()

            } catch (e: Exception) {

                 response = ChatCreateResponse.newBuilder().apply {
                    failureBuilder.apply {
                        this.message = e.message
                        this.internalErrorBuilder
                    }
                }.build()



                message.replyTo?.let { replySubject ->
                    natsConnection.publish(replySubject,
                        NatsValidMongoChatParser.serializeCreateChatResponse(response))
                }
            }

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject,
                    NatsValidMongoChatParser.serializeCreateChatResponse(response))
            }
        }
    }

}
