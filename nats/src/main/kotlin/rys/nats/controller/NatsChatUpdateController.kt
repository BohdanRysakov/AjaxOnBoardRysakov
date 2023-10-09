package rys.nats.controller

import io.nats.client.Connection
import io.nats.client.Message
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Component
import rys.nats.exception.InternalException
import rys.nats.natsservice.ProtobufService
import rys.nats.protostest.Mongochat
import rys.nats.protostest.Mongochat.ChatDeleteResponse.Success
import rys.rest.model.MongoChat
import rys.rest.service.ChatService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


@Component
class NatsChatUpdateController(
    private val natsConnection: Connection,
    private val chatService: ChatService,
    private val protoService: ProtobufService

) {
    @PostConstruct
    fun init() {

        val response: CompletableFuture<Message> = natsConnection.request("chat.update", Mongochat.ChatDeleteRequest.newBuilder().build().toByteArray())

        val resp: Message = response.get(10, TimeUnit.SECONDS)
        val obj = Mongochat.ChatDeleteResponse.parseFrom(resp.data)

        if(obj.hasSuccess())
        if(obj.hasFailure())


        natsConnection.createDispatcher().subscribe("chat.update") { message ->

            var idToUpdate: String? = null
            var updatedChat: MongoChat? = null

            var response : MongoChat? = null



            try {
                val result: Pair<String, MongoChat> = protoService.deserializeUpdateRequest(message.data)
                idToUpdate = result.first
                updatedChat = result.second

                response = chatService.updateChat(ObjectId(idToUpdate), updatedChat)

                val successRespose = Mongochat.ChatDeleteResponse.newBuilder()
                    .setSuccess(Success.newBuilder().setResult(true))
                    .build()

                natsConnection.publish(replySubject, successRespose.toByteArray()) // publish success response

                natsConnection.publish(replySubject, failureResponse) // publish success response
            } catch (e: Exception){
                message.replyTo?.let { replySubject ->

                    val failure = Mongochat.ChatDeleteResponse.newBuilder().apply {

                        failureBuilder.message = "ssss" // take from exception
                        when(e) {
                            is InternalException -> failureBuilder.serializeErrorBuilder
                            is NotFoundException -> failureBuilder.notFoundBuilder
                        }

                    }.build()

                    when(e) {
                        is InternalException -> {
                            // build failure response
                            val failureResult = Mongochat.ChatDeleteResponse.newBuilder()
                                .setFailure(
                                    Mongochat.Failure.newBuilder()
                                        .setMessage(e.message)
                                        .setSerializeError(Mongochat.Error.getDefaultInstance())
                                )
                                .build()


                            natsConnection.publish(replySubject, failureResult.toByteArray())
                            // build failure response
                        }

                        is NotFoundException -> {
                            // build failure response
                            val failureResult = Mongochat.ChatDeleteResponse.newBuilder()
                                .setFailure(
                                    Mongochat.Failure.newBuilder()
                                        .setMessage(e.message)
                                        .setNotFound(Mongochat.Error.getDefaultInstance())
                                )
                                .build()


                            natsConnection.publish(replySubject, failureResult.toByteArray())
                            // build failure response
                        }
                    }
                    when(e) {
                        is InternalException -> {
                            // build failure response
                            val failureResponse: ByteArray = ....
                            natsConnection.publish(replySubject, failureResponse)
                            // build failure response
                        }
                    }

                    natsConnection.publish(replySubject, protoService.serializeChat(null))
                }
            }

            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, protoService.serializeChat(response))
            }
        }
    }
}
