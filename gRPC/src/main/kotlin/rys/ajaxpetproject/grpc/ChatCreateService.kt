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
import rys.ajaxpetproject.service.chat.ReactorChatCreateServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.utils.toProto

@GrpcService
class ChatCreateService(private val chatService: ChatService) :
    ReactorChatCreateServiceGrpc.ChatCreateServiceImplBase() {

    override fun createChat(request: ChatCreateRequest): Mono<ChatCreateResponse> {

        if (!request.hasChat()) {
            logger.info("Received empty request")
            return buildFailureResponse(BadRequestException("Bad request")).toMono()
        }
        logger.info("Received request to create chat: $request")

        return chatService.save(request.chat.toModel())
            .map { buildSuccessResponse(it) }
            .onErrorResume { buildFailureResponse(it).toMono() }
    }

    private fun buildSuccessResponse(chat: MongoChat): ChatCreateResponse {

        return ChatCreateResponse.newBuilder().apply {
            successBuilder.result = chat.toProto()
        }.build()

    }

    private fun buildFailureResponse(e: Throwable): ChatCreateResponse {
        logger.error("Error while creating chat: ${e.message}", e)

        return ChatCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatCreateService::class.java)
    }
}
