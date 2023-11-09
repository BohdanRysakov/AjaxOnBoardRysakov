package rys.ajaxpetproject.chat.infractructure.grpc

import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.`in`.IChatServiceInPort
import rys.ajaxpetproject.chat.domain.entity.Chat
import rys.ajaxpetproject.commonmodels.chat.proto.Chat as ProtoChat
import rys.ajaxpetproject.exceptions.BadRequestException
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.service.chat.ReactorChatServiceGrpc

@GrpcService
class ChatGrpcService(private val chatService: IChatServiceInPort)
    : ReactorChatServiceGrpc.ChatServiceImplBase() {
    override fun create(request: ChatCreateRequest): Mono<ChatCreateResponse> {
        if (!request.hasChat()) {
            logger.info("Received empty request")
            return createFailureResponse(BadRequestException("Bad request")).toMono()
        }
        logger.info("Received request to create chat: {}", request)

        return chatService.save(request.chat.toModel())
            .map {
                createSuccessResponse(it)
            }
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

    private fun ProtoChat.toModel(): Chat {
        return Chat(
            id = id,
            name = name,
            users = usersList,
            messages = messagesList
        )
    }

    private fun Chat.toProto(): ProtoChat {
        val chat = this@toProto
        return ProtoChat.newBuilder().apply {
            this.id = chat.id.toString()
            this.name = chat.name
            addAllUsers(chat.users)
            addAllMessages(chat.messages)
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChatGrpcService::class.java)
    }
}
