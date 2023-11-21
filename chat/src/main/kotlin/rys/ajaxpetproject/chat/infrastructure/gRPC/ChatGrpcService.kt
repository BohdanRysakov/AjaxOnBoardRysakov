package rys.ajaxpetproject.chat.infrastructure.gRPC

import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.mapper.createEvent
import rys.ajaxpetproject.chat.application.port.input.ChatServiceInPort
import rys.ajaxpetproject.chat.application.port.output.MessageAddEventOutPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent
import rys.ajaxpetproject.chat.infrastructure.gRPC.exceptions.BadRequestException
import rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent.mapper.toDomainModel
import rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent.mapper.toProto
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription
import rys.ajaxpetproject.service.chat.ReactorChatServiceGrpc

@GrpcService
class ChatGrpcService(
    private val chatService: ChatServiceInPort,
    private val messageEventService: MessageAddEventOutPort
) : ReactorChatServiceGrpc.ChatServiceImplBase() {

    override fun subscribe(request: EventSubscription.CreateSubscriptionRequest):
            Flux<EventSubscription.CreateSubscriptionResponse> {
        logger.info("Received subscription request for chat {}", request.chatId)
        return Flux.concat(
            chatService.getMessagesInChat(request.chatId).flatMap { message ->
                val messageDto = message.createEvent(request.chatId)
                val response = buildSuccessResponse(messageDto)
                response.toMono()
            },
            messageEventService.publishMessageCreatedEvent(request.chatId)
                .flatMap { messageAddedEvent: MessageAddedEvent ->
                    val response = buildSuccessResponse(messageAddedEvent)
                    response.toMono()
                }
        )
            .onErrorResume { e -> Flux.just(buildFailureResponse(e.message)) }
    }

    private fun buildSuccessResponse(messageAddedEvent: MessageAddedEvent):
        EventSubscription.CreateSubscriptionResponse {
        return EventSubscription.CreateSubscriptionResponse.newBuilder().apply {
            successBuilder.messageDtoBuilder.apply {
                chatId = messageAddedEvent.chatId
                message = messageAddedEvent.message.toProto()
            }

        }.build()
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
