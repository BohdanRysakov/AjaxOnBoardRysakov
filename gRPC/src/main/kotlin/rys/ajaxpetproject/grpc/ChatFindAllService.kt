package rys.ajaxpetproject.grpc

import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneRequest
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.chat.ReactorChatFindAllServiceGrpc
import rys.ajaxpetproject.utils.toProto

//Made for gRPC stream Demo purpose
@GrpcService
class ChatFindAllService(private val chatService: ChatService) :
    ReactorChatFindAllServiceGrpc.ChatFindAllServiceImplBase() {

    override fun chatFindAll(request: ChatFindOneRequest): Flux<ChatFindOneResponse> {
        return chatService.findAll()
            .flatMap { item ->
                buildSuccessResponse(item)
            }
            .onErrorResume { buildFailureResponse(it).toMono() }
    }

    private fun buildSuccessResponse(chat: MongoChat): Mono<ChatFindOneResponse> {
        return ChatFindOneResponse.newBuilder().apply {
            this.successBuilder.result = chat.toProto()
        }.build().toMono()
    }

    private fun buildFailureResponse(e: Throwable): ChatFindOneResponse {
        logger.error("Error while creating chat: ${e.message}", e)
        return ChatFindOneResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatFindAllService::class.java)
    }
}
