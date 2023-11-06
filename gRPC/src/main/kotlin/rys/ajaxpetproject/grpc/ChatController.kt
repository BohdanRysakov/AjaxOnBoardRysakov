package rys.ajaxpetproject.grpc

import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.exceptions.BadRequestException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneRequest
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.chat.ReactorChatServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.utils.toProto

@GrpcService
class ChatController(private val chatService: ChatService) :
    ReactorChatServiceGrpc.ChatServiceImplBase() {

    override fun findAll(request: ChatFindOneRequest): Flux<ChatFindOneResponse> {
        return chatService.findAll()
            .map { item ->
                findAllSuccessResponse(item)
            }
            .onErrorResume { findAllFailureResponse(it).toMono() }
    }

    override fun create(request: ChatCreateRequest): Mono<ChatCreateResponse> {
        if (!request.hasChat()) {
            logger.info("Received empty request")
            return createFailureResponse(BadRequestException("Bad request")).toMono()
        }
        logger.info("Received request to create chat: $request")

        return chatService.save(request.chat.toModel())
            .map { createSuccessResponse(it) }
            .onErrorResume { createFailureResponse(it).toMono() }
    }

    private fun createSuccessResponse(chat: MongoChat): ChatCreateResponse {
        return ChatCreateResponse.newBuilder().apply {
            successBuilder.result = chat.toProto()
        }.build()

    }

    private fun createFailureResponse(e: Throwable): ChatCreateResponse {
        logger.error("Error while creating chat: ${e.message}", e)

        return ChatCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    private fun findAllSuccessResponse(chat: MongoChat): ChatFindOneResponse {
        return ChatFindOneResponse.newBuilder().apply {
            this.successBuilder.result = chat.toProto()
        }.build()
    }

    private fun findAllFailureResponse(e: Throwable): ChatFindOneResponse {
        logger.error("Error while creating chat: ${e.message}", e)
        return ChatFindOneResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatController::class.java)
    }
}
