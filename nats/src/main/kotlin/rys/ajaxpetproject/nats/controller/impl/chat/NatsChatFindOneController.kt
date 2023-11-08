package rys.ajaxpetproject.nats.controller.impl.chat

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.nats.controller.NatsController
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneRequest
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneResponse
import rys.ajaxpetproject.internalapi.ChatSubjectsV1
import rys.ajaxpetproject.utils.toProto

@Component
@Suppress("NestedBlockDepth")
class NatsChatFindOneController(
    override val connection: Connection,
    private val chatService: ChatService
) : NatsController<ChatFindOneRequest, ChatFindOneResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.FIND_ONE
    override val parser: Parser<ChatFindOneRequest> = ChatFindOneRequest.parser()

    override fun handle(request: ChatFindOneRequest): Mono<ChatFindOneResponse> {
        return chatService
            .findChatById(request.id)
            .map { buildSuccessResponse(it) }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }
    }

    private fun buildSuccessResponse(chat: MongoChat): ChatFindOneResponse =
        ChatFindOneResponse.newBuilder().apply {
            successBuilder.result = chat.toProto()
        }.build()

    private fun buildFailureResponse(e: Throwable): ChatFindOneResponse {
        logger.error("Error while creating chat: {}", e.message, e)
        return ChatFindOneResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatFindOneController::class.java)
    }
}
