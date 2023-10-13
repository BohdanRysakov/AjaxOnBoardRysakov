package rys.ajaxpetproject.nats.controller.impl

import com.google.protobuf.Parser
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.commonmodels.chat.proto.Chat
import rys.ajaxpetproject.nats.controller.NatsController
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.nats.exception.InternalException
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneRequest
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneResponse
import rys.ajaxpetproject.subjects.ChatSubjectsV1

@Component
@Suppress("NestedBlockDepth")
class NatsChatFindOneController(
    override val connection: Connection,
    private val chatService: ChatService
) : NatsController<ChatFindOneRequest, ChatFindOneResponse> {

    override val subject = ChatSubjectsV1.ChatRequest.FIND_ONE
    override val parser: Parser<ChatFindOneRequest> = ChatFindOneRequest.parser()

    override fun handle(request: ChatFindOneRequest): ChatFindOneResponse = runCatching {
        val chatId  = request.id
        val chat : MongoChat = chatService.findChatById(ObjectId(chatId))
            ?: throw InternalException("Chat not found")
        buildSuccessResponse(chat)
    }.getOrElse {
        buildFailureResponse(it)
    }

    private fun buildSuccessResponse(chat: MongoChat): ChatFindOneResponse =
         ChatFindOneResponse.newBuilder().apply {
            successBuilder.apply {
                this.result = Chat.newBuilder().apply {
                    id = chat.id.toString()
                    name = chat.name
                    chat.users.forEach {
                        this.addUsers(it.toString())
                    }
                }.build()
            }
        }.build()

    private fun buildFailureResponse(e:Throwable): ChatFindOneResponse {
        logger.error("Error while creating chat: ${e.message}", e)
        return ChatFindOneResponse.newBuilder().apply {
            failureBuilder.message = e.message
            failureBuilder.internalErrorBuilder
        }.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsChatFindOneController::class.java)
    }
}
