package rys.ajaxpetproject.chat.infrastructure.nats.controller.chat

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.input.ChatServiceInPort
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteRequest
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteResponse
import rys.ajaxpetproject.internalapi.ChatSubjectsV1

@Component
class NatsChatDeleteController(
    override val connection: Connection,
    private val chatService: ChatServiceInPort
) : NatsController<ChatDeleteRequest, ChatDeleteResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.DELETE
    override val parser: Parser<ChatDeleteRequest> = ChatDeleteRequest.parser()

    override fun handle(request: ChatDeleteRequest): Mono<ChatDeleteResponse> {
        val idToDelete = request.requestId

        return chatService
            .delete(idToDelete)
            .map { buildSuccessResponse() }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }
    }

    private fun buildSuccessResponse(): ChatDeleteResponse =
        ChatDeleteResponse.newBuilder().apply {
            successBuilder.result = true
        }.build()

    private fun buildFailureResponse(e: Throwable): ChatDeleteResponse {
        logger.error("Error while deleting chat: {}", e.message, e)

        return ChatDeleteResponse.newBuilder().apply {
            failureBuilder
                .setMessage(e.message)
                .internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatDeleteController::class.java)
    }
}
