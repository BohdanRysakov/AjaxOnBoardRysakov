package rys.ajaxpetproject.chat.infrastructure.nats.controller.chat

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.input.ChatServiceInPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.infrastructure.nats.mapper.toDomainModel
import rys.ajaxpetproject.chat.infrastructure.nats.mapper.toProto
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.internalapi.ChatSubjectsV1

@Service
@Suppress("NestedBlockDepth")
class NatsChatCreationController(
    override val connection: Connection,
    private val chatService: ChatServiceInPort
) : NatsController<ChatCreateRequest, ChatCreateResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.CREATE

    override val parser: Parser<ChatCreateRequest> = ChatCreateRequest.parser()

    override fun handle(request: ChatCreateRequest): Mono<ChatCreateResponse> {

        val chat: Chat = request.chat.toDomainModel()

        return chatService
            .save(chat)
            .map { newChat -> buildSuccessResponse(newChat) }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }
    }

    private fun buildSuccessResponse(chat: Chat): ChatCreateResponse {
        return ChatCreateResponse.newBuilder().apply {
            successBuilder.result = chat.toProto()
        }.build()

    }

    private fun buildFailureResponse(e: Throwable): ChatCreateResponse {
        logger.error("Error while creating chat: {}", e.message, e)

        return ChatCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatCreationController::class.java)
    }
}
