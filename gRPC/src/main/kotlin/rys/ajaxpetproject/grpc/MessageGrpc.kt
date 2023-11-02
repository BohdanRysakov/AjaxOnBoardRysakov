package rys.ajaxpetproject.grpc

import io.nats.client.Connection
import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.request.message.create.proto.MessageCreateResponse
import rys.ajaxpetproject.request.message.create.proto.MessageCreateRequest
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.message.ReactorMessageServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionRequest
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionResponse

@GrpcService
@Suppress("TooGenericExceptionCaught")
class MessageGrpc(
    private val natsConnection: Connection,
    private val chatService: ChatService,
    private val messageService: MessageService,
) : ReactorMessageServiceGrpc.MessageServiceImplBase() {

    private val messageParser = MessageDto.parser()

    override fun create(request: MessageCreateRequest): Mono<MessageCreateResponse> {
        return request.message.toModel().let {
            messageService.create(it)
        }.flatMap {
            chatService.addMessage(it.id!!, request.chatId)
        }
            .then(createSuccessResponse().toMono())
            .onErrorResume { createFailureResponse(it).toMono() }

    }

    override fun subscribe(requests: Flux<CreateSubscriptionRequest>): Flux<CreateSubscriptionResponse> {
        logger.info("Received request to subscribe to chats")

        return requests.flatMap { request: CreateSubscriptionRequest ->
            Flux.merge(request.chatList.map { chatId ->
                subscribeToChat(chatId)
            })
        }.onErrorResume { e -> Flux.just(buildFailureResponse(e.message)) }
    }

    private fun subscribeToChat(chatId: String): Flux<CreateSubscriptionResponse> {
        return Flux.create { sink ->
            val subject = MessageEvent.createMessageCreateNatsSubject(chatId)

            val dispatcher = natsConnection.createDispatcher { msg ->

                try {
                    val messageDto = messageParser.parseFrom(msg.data)
                    val response = buildSuccessResponse(messageDto)
                    sink.next(response)
                } catch (e: Exception) {
                    sink.error(e)
                }
            }.subscribe(subject)

            sink.onDispose {
                dispatcher.unsubscribe(subject)
            }
        }
    }

    private fun buildSuccessResponse(messageDto: MessageDto): CreateSubscriptionResponse {
        return CreateSubscriptionResponse.newBuilder().apply {
            successBuilder.messageDto = messageDto
        }.build()
    }

    private fun buildFailureResponse(message: String?): CreateSubscriptionResponse {
        return CreateSubscriptionResponse.newBuilder().apply {
            message?.let { failureBuilder.message = it }
            failureBuilder.errorBuilder
        }.build()
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
