package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import rys.nats.natsservice.ProtobufService
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsValidMongoChatParser
import rys.nats.utils.NatsValidMongoChatParser.deserializeFindChatRequest
import rys.rest.model.MongoChat
import rys.rest.service.ChatService


@Component
class NatsChatFindOneController(
    private val natsConnection: Connection,
    private val chatService: ChatService,
    private val protoService : ProtobufService
) {
    @PostConstruct
    fun init() {
        val dispatcher: Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.findOne") { message ->

            var idToFind :String? = null
            var chat : MongoChat? = null

            try {
                idToFind = deserializeFindChatRequest(message.data)
                throw Exception("test")
               // chat = chatService.findChatById(ObjectId(idToFind))
            }
            catch (e: Exception){
                message.replyTo?.let { replySubject ->
                    natsConnection.publish(replySubject, NatsValidMongoChatParser.serializeFindChatResponse
                        (Mongochat.ChatFindOneResponse.newBuilder().apply {
                            failure = Mongochat.ChatFindOneResponse.Failure.newBuilder().apply {
                                this.message = e.message
                            }.build()
                        }.build()
                        ))
                    }

                }




            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, protoService.serializeChat(chat))
            }
        }
    }
}
