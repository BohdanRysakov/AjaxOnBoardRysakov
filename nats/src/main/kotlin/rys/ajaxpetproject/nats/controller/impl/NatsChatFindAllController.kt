package rys.ajaxpetproject.nats.controller.impl

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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
): NatsController<ChatFindAllRequest, ChatFindAllResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.FIND_ALL
    override val parser: Parser<ChatFindAllRequest> = ChatFindAllRequest.parser()

    override fun handle(request: ChatFindAllRequest): ChatFindAllResponse = runCatching {
        val chats: List<MongoChat> = chatService.findAllChats()
        buildSuccessResponse(chats)
    }.getOrElse {
        buildFailureResponse(it)
    }

    private fun buildSuccessResponse(chats: List<MongoChat>): ChatFindAllResponse =
        ChatFindAllResponse.newBuilder().apply {
            successBuilder.apply {
                chats.forEach { chat ->
                    this.addResult(
                        Chat.newBuilder().apply {
                            id = chat.id.toString()
                            name = chat.name
                            chat.users.forEach {
                                this.addUsers(it.toString())
                            }
                        }.build()
                    )
                }
            }
        }.build()

    private fun buildFailureResponse(e:Throwable): ChatFindAllResponse {
        logger.error("Error while creating chat: ${e.message}", e)
        return ChatFindAllResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.internalErrorBuilder
            }
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatFindAllController::class.java)
    }
}
