package rys.ajaxpetproject.chat.infrastructure.nats.controller.chat

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.inn.ChatServiceInPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.infrastructure.mapper.toProto
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllRequest
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllResponse
import rys.ajaxpetproject.internalapi.ChatSubjectsV1

@Service
class NatsChatFindAllController(
    override val connection: Connection,
    private val chatService: ChatServiceInPort
) : NatsController<ChatFindAllRequest, ChatFindAllResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.FIND_ALL
    override val parser: Parser<ChatFindAllRequest> = ChatFindAllRequest.parser()

    override fun handle(request: ChatFindAllRequest): Mono<ChatFindAllResponse> {
        return chatService
            .findAll()
            .collectList()
            .map { chats -> buildSuccessResponse(chats) }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }

    }

    private fun buildSuccessResponse(chats: List<Chat>): ChatFindAllResponse {
        return ChatFindAllResponse.newBuilder().apply {
            successBuilder.also { success ->
                chats.map { chat ->
                    chat.toProto()
                }
                    .fold(success) { result: ChatFindAllResponse.Success.Builder, chat ->
                        result.addResult(chat)
                    }
            }
        }.build()
    }

    private fun buildFailureResponse(e: Throwable): ChatFindAllResponse {
        logger.error("Error while creating chat: {}", e.message, e)
        return ChatFindAllResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatFindAllController::class.java)
    }
}
