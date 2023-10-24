package rys.ajaxpetproject.nats.controller.impl

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.commonmodels.chat.proto.Chat
import rys.ajaxpetproject.nats.controller.NatsController
import rys.ajaxpetproject.nats.exception.InternalException
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateRequest
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.subjects.ChatSubjectsV1

@Component
@Suppress("NestedBlockDepth")
class NatsChatUpdateController(
    override val connection: Connection,
    private val chatService: ChatService
) : NatsController<ChatUpdateRequest, ChatUpdateResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.UPDATE
    override val parser: Parser<ChatUpdateRequest> = ChatUpdateRequest.parser()

    override fun reply(request: ChatUpdateRequest): Mono<ChatUpdateResponse> {
        val targetId = ObjectId(request.requestId)
        val newChat = MongoChat(
            id = ObjectId(request.chat.id.toString()),
            name = request.chat.name,
            users = request.chat.usersList.map { ObjectId(it) },
            messages = request.chat.messagesList.map { ObjectId(it) })

        return chatService
            .update(targetId, newChat)
            .flatMap { updatedChat -> buildSuccessResponse(updatedChat).toMono() }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }
    }

    private fun buildSuccessResponse(updatedChat: MongoChat): ChatUpdateResponse =
        ChatUpdateResponse.newBuilder().apply {
            successBuilder.apply {
                this.result = Chat.newBuilder().apply {
                    id = updatedChat.id.toString()
                    name = updatedChat.name
                    updatedChat.users.forEach {
                        this.addUsers(it.toString())
                    }
                }.build()
            }
        }.build()

    private fun buildFailureResponse(e: Throwable): ChatUpdateResponse {
        logger.error("Error while creating chat: ${e.message}", e)

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
