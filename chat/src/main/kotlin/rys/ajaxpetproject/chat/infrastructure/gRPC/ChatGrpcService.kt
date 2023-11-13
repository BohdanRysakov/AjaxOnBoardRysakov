package rys.ajaxpetproject.chat.infrastructure.gRPC

import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.`in`.ChatServiceInPort
import rys.ajaxpetproject.chat.application.port.out.MessageAddEventOutPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.infrastructure.adapter.InitialStateEventLoader
import rys.ajaxpetproject.chat.infrastructure.mapper.toDomainModel
import rys.ajaxpetproject.chat.infrastructure.mapper.toProto
import rys.ajaxpetproject.internalapi.exceptions.BadRequestException
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription
import rys.ajaxpetproject.service.chat.ReactorChatServiceGrpc

@GrpcService
class ChatGrpcService(
    private val chatService: ChatServiceInPort,
    private val messageEventService: MessageAddEventOutPort,
    private val initialStateEventLoader: InitialStateEventLoader
) : ReactorChatServiceGrpc.ChatServiceImplBase() {

    override fun subscribe(request: EventSubscription.CreateSubscriptionRequest):
            Flux<EventSubscription.CreateSubscriptionResponse> {
        logger.info("Received subscription request for chat {}", request.chatId)
        return Flux.concat(
            initialStateEventLoader.loadInitialState(request.chatId),
            messageEventService.publishMessageCreatedEvent(request.chatId)
        )
            .onErrorResume { e -> Flux.just(buildFailureResponse(e.message)) }
    }

    private fun buildFailureResponse(message: String?): EventSubscription.CreateSubscriptionResponse {
        return EventSubscription.CreateSubscriptionResponse.newBuilder().apply {
            message?.let { failureBuilder.message = it }
            failureBuilder.errorBuilder
        }.build()
    }


    override fun create(request: ChatCreateRequest): Mono<ChatCreateResponse> {
        if (!request.hasChat()) {
            logger.info("Received empty request")
            return createFailureResponse(BadRequestException("Bad request")).toMono()
        }
        logger.info("Received request to create chat: {}", request)

        return chatService.save(request.chat.toDomainModel())
            .map { createSuccessResponse(it) }
            .onErrorResume { createFailureResponse(it).toMono() }
    }

    private fun createSuccessResponse(chat: Chat): ChatCreateResponse {
        return ChatCreateResponse.newBuilder().apply {
            successBuilder.result = chat.toProto()
        }.build()
    }

    private fun createFailureResponse(e: Throwable): ChatCreateResponse {
        logger.error("Error while creating chat: {}", e.message, e)

        return ChatCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatGrpcService::class.java)
    }
}
