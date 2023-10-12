package rys.ajaxpetproject.nats.controller.impl

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rys.ajaxpetproject.nats.controller.NatsController
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteRequest
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.subjects.ChatSubjectsV1

@Component
class NatsChatDeleteController(
    override val connection: Connection,
    private val chatService: ChatService
) : NatsController<ChatDeleteRequest, ChatDeleteResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.DELETE
    override val parser: Parser<ChatDeleteRequest> = ChatDeleteRequest.parser()

    override fun handle(request: ChatDeleteRequest): ChatDeleteResponse = runCatching {
        val idToDelete = request.requestId
        chatService.deleteChat(ObjectId(idToDelete))
        buildSuccessResponse()
    }.getOrElse {
        buildFailureResponse(it)
    }

    private fun buildSuccessResponse(): ChatDeleteResponse =
        ChatDeleteResponse.newBuilder().apply {
            successBuilder.apply {
                this.result = true
            }
        }.build()

    private fun buildFailureResponse(e: Throwable): ChatDeleteResponse {
        logger.error("Error while deleting chat: ${e.message}", e)

        return ChatDeleteResponse.newBuilder().setFailure(
            ChatDeleteResponse.Failure.newBuilder().apply {
                this.message = e.message
                this.internalErrorBuilder
            }.build()
        ).build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatDeleteController::class.java)
    }
}
