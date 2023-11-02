package rys.ajaxpetproject.grpc

import io.nats.client.Connection
import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.util.context.Context
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.request.message.create.proto.MessageCreateResponse
import rys.ajaxpetproject.request.message.create.proto.MessageCreateRequest
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.message.ReactorMessageServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionRequest
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionResponse
import rys.ajaxpetproject.utils.toProto

@GrpcService
@Suppress("TooGenericExceptionCaught")
class MessageGrpc(
    natsConnection: Connection,
    private val chatService: ChatService,
    private val messageService: MessageService,
) : ReactorMessageServiceGrpc.MessageServiceImplBase() {

    private val messageParser = MessageDto.parser()
    private val dispatcher = natsConnection.createDispatcher()

    override fun subscribe(requests: Flux<CreateSubscriptionRequest>): Flux<CreateSubscriptionResponse> {
        logger.info("Received request to subscribe to chats")

        return requests.flatMap { request: CreateSubscriptionRequest ->
            request.chatList.toFlux()
                .flatMap { chatId ->
                    Flux.deferContextual {
                        if (!it.get<MutableSet<String>>("chatsSet").contains(chatId)) {
                            Flux.concat(
                                loadInitialState(chatId),
                                subscribeToChat(chatId)
                            )
                        } else {
                            Flux.empty()
                        }
                    }
                }
        }.contextWrite(Context.of("chatsSet", mutableSetOf<String>()))
            .onErrorResume { e -> Flux.just(buildFailureResponse(e.message)) }
    }

    private fun loadInitialState(chatId: String): Flux<CreateSubscriptionResponse> {
        return Flux.deferContextual { ctx ->
            val chatsSet = ctx.get<MutableSet<String>>("chatsSet")
            if (!chatsSet.contains(chatId)) {
                chatService.getMessagesInChat(chatId)
                    .flatMap { message ->
                        chatsSet.add(chatId)
                        val messageDto = message.toDto(chatId)
                        val response = buildSuccessResponse(messageDto)
                        Mono.just(response)
                    }
            } else {
                Flux.empty()
            }
        }

    }

    private fun subscribeToChat(chatId: String): Flux<CreateSubscriptionResponse> {
        return Flux.create { sink ->
            val subject = MessageEvent.createMessageCreateNatsSubject(chatId)

            dispatcher.apply {
                subscribe(subject) { msg ->
                    try {
                        val messageDto = messageParser.parseFrom(msg.data)
                        val response = buildSuccessResponse(messageDto)
                        sink.next(response)
                    } catch (e: Exception) {
                        sink.error(e)
                    }
                }

            }

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

    fun MongoMessage.toDto(chatId: String): MessageDto {
        val message = this
        return MessageDto.newBuilder().apply {
            this.message = message.toProto()
            this.chatId = chatId
        }.build()
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(MessageGrpc::class.java)
    }
}
