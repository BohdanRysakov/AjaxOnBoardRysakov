package rys.ajaxpetproject.grpc

import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.exceptions.BadRequestException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.chat.ReactorChatServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.utils.toProto

@GrpcService
class ChatGrpcService(private val chatService: ChatService) :
    ReactorChatServiceGrpc.ChatServiceImplBase() {

    override fun create(request: ChatCreateRequest): Mono<ChatCreateResponse> {
        if (!request.hasChat()) {
            logger.info("Received empty request")
            return createFailureResponse(BadRequestException("Bad request")).toMono()
        }
        logger.info("Received request to create chat: {}", request)

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
