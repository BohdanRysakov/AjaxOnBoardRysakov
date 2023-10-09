package rys.nats.controller

import com.google.protobuf.ProtocolStringList
import io.nats.client.Connection
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import rys.nats.exception.InternalException
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

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        natsConnection.createDispatcher().subscribe("chat.create") { message ->
            try {

                val requestBody: Mongochat.Chat = NatsValidMongoChatParser
                    .deserializeCreateChatRequest(message.data).chat

                val newChat = chatService.createChat(MongoChat(
                    id = ObjectId(requestBody.id),
                    name = requestBody.name,
                    users = requestBody.usersList.map { ObjectId(it) }
                ))

                val response = ChatCreateResponse.newBuilder().apply {
                    successBuilder.apply {
                        this.result = Mongochat.Chat.newBuilder().apply {
                            id = newChat.id.toString()
                            name = newChat.name
                            newChat.users.forEach {
                                this.addUsers(it.toString())
                            }
                        }.build()
                    }
                }.build()

                message.replyTo?.let { replySubject ->
                    natsConnection.publish(
                        replySubject,
                        NatsValidMongoChatParser.serializeCreateChatResponse(response)
                    )
                }

            } catch (e: Exception) {

                logger.error("Error while creating chat: ${e.message}", e)

                val response = ChatCreateResponse.newBuilder().apply {
                    failureBuilder.apply {
                        this.message = e.message
                        this.internalErrorBuilder
                    }
                }.build()
                message.replyTo?.let { replySubject ->
                    natsConnection.publish(
                        replySubject,
                        NatsValidMongoChatParser.serializeCreateChatResponse(response)
                    )
                }
            }
        }
    }

}
