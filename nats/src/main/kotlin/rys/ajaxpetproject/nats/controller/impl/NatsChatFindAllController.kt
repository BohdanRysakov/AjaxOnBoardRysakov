package rys.ajaxpetproject.nats.controller.impl

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.commonmodels.chat.proto.Chat
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.nats.controller.NatsController
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllRequest
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllResponse
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.subjects.ChatSubjectsV1

@Service
class NatsChatFindAllController(
    override val connection: Connection,
    private val chatService: ChatService
) : NatsController<ChatFindAllRequest, ChatFindAllResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.FIND_ALL
    override val parser: Parser<ChatFindAllRequest> = ChatFindAllRequest.parser()

    override fun reply(request: ChatFindAllRequest): Mono<ChatFindAllResponse> {
        return chatService
            .findAll()
            .collectList()
            .map { chats -> buildSuccessResponse(chats.toList()) }
            .doOnError { logger.error("***") }
            .onErrorResume { e -> buildFailureResponse(e).toMono() }

    }

    private fun buildSuccessResponse(chats: List<MongoChat>): ChatFindAllResponse {


        val chatsResponse = chats.map { chat: MongoChat ->
            Chat.newBuilder().apply {
                id = chat.id.toString()
                name = chat.name

                addAllMessages(chat.messages.map { it.toString() })
//                addAllUsers(chat.users.map { it.toString() })

//                chat.users.forEach { userId: ObjectId ->
//                    this.addUsers(userId.toString())
//                }
//                chat.messages.forEach { messageId ->
//                    this.addMessages(messageId.toHexString())
//                }
            }.build()
        }

        ChatFindAllResponse.Success.newBuilder().apply {

        }
        return ChatFindAllResponse.newBuilder().apply {
            successBuilder.addAllResult(chatsResponse)
        }.build()
    }

    private fun buildFailureResponse(e: Throwable): ChatFindAllResponse {
        logger.error("Error while creating chat: ${e.message}", e)
        return ChatFindAllResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatFindAllController::class.java)
    }
}
