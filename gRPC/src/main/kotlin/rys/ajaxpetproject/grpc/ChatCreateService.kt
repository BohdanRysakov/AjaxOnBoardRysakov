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
class ChatCreateService(private val chatService: ChatService) :
    ReactorChatServiceGrpc.ChatServiceImplBase() {

    override fun chatFindAll(request: ChatFindOneRequest): Flux<ChatFindOneResponse> {
        return chatService.findAll()
            .map { item ->
                chatFindAllBuildSuccessResponse(item)
            }
            .onErrorResume { chatFindAllBuildFailureResponse(it).toMono() }
    }

    override fun createChat(request: ChatCreateRequest): Mono<ChatCreateResponse> {
        if (!request.hasChat()) {
            logger.info("Received empty request")
            return chatCreateBuildFailureResponse(BadRequestException("Bad request")).toMono()
        }
        logger.info("Received request to create chat: $request")

        return chatService.save(request.chat.toModel())
            .map { chatCreateBuildSuccessResponse(it) }
            .onErrorResume { chatCreateBuildFailureResponse(it).toMono() }
    }

    private fun chatCreateBuildSuccessResponse(chat: MongoChat): ChatCreateResponse {
        return ChatCreateResponse.newBuilder().apply {
            successBuilder.result = chat.toProto()
        }.build()

    }

    private fun chatCreateBuildFailureResponse(e: Throwable): ChatCreateResponse {
        logger.error("Error while creating chat: ${e.message}", e)

        return ChatCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    private fun chatFindAllBuildSuccessResponse(chat: MongoChat): ChatFindOneResponse {
        return ChatFindOneResponse.newBuilder().apply {
            this.successBuilder.result = chat.toProto()
        }.build()
    }

    private fun chatFindAllBuildFailureResponse(e: Throwable): ChatFindOneResponse {
        logger.error("Error while creating chat: ${e.message}", e)
        return ChatFindOneResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatCreateService::class.java)
    }
}
