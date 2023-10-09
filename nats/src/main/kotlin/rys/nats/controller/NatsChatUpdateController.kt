package rys.nats.controller

import io.nats.client.Connection
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat
import rys.rest.service.ChatService


@Component
class NatsChatUpdateController(
    private val natsConnection: Connection,
    private val chatService: ChatService

) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        natsConnection.createDispatcher().subscribe("chat.update") { message ->

            try {

                val requestBody = NatsValidMongoChatParser.deserializeUpdateRequest(message.data)

                val targetId = ObjectId(requestBody.requestId)

                val newChat = MongoChat(id = null,
                    name = requestBody.chat.name,
                    users = requestBody.chat.usersList.map { ObjectId(it) })

                val chat = chatService.updateChat(targetId, newChat)

                val response = Mongochat.ChatUpdateResponse.newBuilder().apply {
                    successBuilder.apply {
                        Mongochat.Chat.newBuilder().apply {
                            id = chat!!.id.toString()
                            name = chat.name
                            usersList.addAll(chat.users.map { it.toString() })
                        }
                    }
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(
                        it,
                        NatsValidMongoChatParser.serializeUpdateResponse(response)
                    )
                }

            } catch (e: Exception) {
                logger.error("Error while updating chat, see internal logs for detail.", e)

                val response = Mongochat.ChatUpdateResponse.newBuilder().apply {
                    failureBuilder.apply {
                        this.message = e.message
                        this.internalErrorBuilder
                    }
                }.build()

                message.replyTo?.let {
                    natsConnection.publish(
                        it,
                        NatsValidMongoChatParser.serializeUpdateResponse(response)
                    )
                }
            }
        }
    }
}
