package rys.ajaxpetproject.chat.infrastructure.nats.controller.chat

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.input.ChatServiceInPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.infrastructure.nats.mapper.toDomainModel
import rys.ajaxpetproject.chat.infrastructure.nats.mapper.toProto
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateRequest
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateResponse
import rys.ajaxpetproject.internalapi.ChatSubjectsV1

@Component
@Suppress("NestedBlockDepth")
class NatsChatUpdateController(
    override val connection: Connection,
    private val chatService: ChatServiceInPort
) : NatsController<ChatUpdateRequest, ChatUpdateResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.UPDATE
    override val parser: Parser<ChatUpdateRequest> = ChatUpdateRequest.parser()

    override fun handle(request: ChatUpdateRequest): Mono<ChatUpdateResponse> {
        val targetId = request.requestId
        val newChat = request.chat.toDomainModel()

        return chatService
            .update(targetId, newChat)
            .map { updatedChat -> buildSuccessResponse(updatedChat) }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }
    }

    private fun buildSuccessResponse(updatedChat: Chat): ChatUpdateResponse =
        ChatUpdateResponse.newBuilder().apply {
            successBuilder.result = updatedChat.toProto()
        }.build()

    private fun buildFailureResponse(e: Throwable): ChatUpdateResponse {
        logger.error("Error while creating chat: {}", e.message, e)

        return ChatUpdateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatUpdateController::class.java)
    }
}
