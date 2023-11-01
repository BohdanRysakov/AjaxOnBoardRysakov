package rys.ajaxpetproject.grpc

import io.nats.client.Connection
import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.request.message.create.proto.MessageCreateResponse
import rys.ajaxpetproject.request.message.create.proto.MessageCreateRequest
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.message.ReactorMessageServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionRequest
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionResponse
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage

@GrpcService
class MessageGrpc(
    private val natsConnection: Connection,
    private val chatService: ChatService,
    private val messageService: MessageService,
) :
    ReactorMessageServiceGrpc.MessageServiceImplBase() {

    private val messageParser = ProtoMessage.parser()

    override fun subscribe(requests: Flux<CreateSubscriptionRequest>):
            Flux<CreateSubscriptionResponse> {

        logger.error("gRPC succesfully invoked subscribe method ")

        return requests
            .handle {

            }

//        return requests.log()
//            .flatMap { request: CreateSubscriptionRequest ->
//                val chatIds = request.chatList
//                logger.error(
//                    "Checking subject - " +
//                            MessageEvent.createMessageCreateNatsSubject(chatIds[0])
//                )
//                Flux.merge(chatIds.map { chatId -> subscribeToChat(chatId) })
//            }
    }

    private fun subscribeToChat(chatId: String): Flux<CreateSubscriptionResponse> {
        return Flux.create { sink ->
            val subject = MessageEvent.createMessageCreateNatsSubject(chatId)

            val dispatcher = natsConnection.createDispatcher { msg ->

                logger.error("Received message in ${subject} - [${msg.data.decodeToString()}]")

                try {
                    val message = messageParser.parseFrom(msg.data)
                    val response = buildResponse(message)
                    sink.next(response)
                } catch (e: Exception) {
                    sink.error(e)
                }
            }

            dispatcher.subscribe(subject)

            sink.onDispose {
                dispatcher.unsubscribe(subject)
            }
        }
    }

    private fun buildResponse(message: ProtoMessage): CreateSubscriptionResponse {
        return CreateSubscriptionResponse.newBuilder().apply {
            this.message = message
        }.build()
    }


    override fun create(request: MessageCreateRequest): Mono<MessageCreateResponse> {
        return request.message.toModel().let {
            messageService.create(it)
        }.flatMap {
            chatService.addMessage(it.id!!, request.chatId)
        }
            .then(createSuccessResponse().toMono())
            .onErrorResume { createFailureResponse(it).toMono() }

    }

    private fun createSuccessResponse(): MessageCreateResponse {
        return MessageCreateResponse.newBuilder().apply {
            successBuilder.apply {
                this.result = "Message created successfully"
            }
        }.build()
    }

    private fun createFailureResponse(e: Throwable): MessageCreateResponse {
        return MessageCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.errorBuilder
            }
        }.build()
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(MessageGrpc::class.java)
    }
}
