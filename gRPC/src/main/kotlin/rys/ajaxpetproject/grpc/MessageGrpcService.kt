package rys.ajaxpetproject.grpc

import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.input.ChatServiceInPort
import rys.ajaxpetproject.request.message.create.proto.MessageCreateResponse
import rys.ajaxpetproject.request.message.create.proto.MessageCreateRequest
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.message.ReactorMessageServiceGrpc
import rys.ajaxpetproject.utils.toModel

@GrpcService
@Suppress("TooGenericExceptionCaught")
class MessageGrpcService(
    private val messageService: MessageService,
    private val chatService: ChatServiceInPort
) : ReactorMessageServiceGrpc.MessageServiceImplBase() {

    override fun create(request: MessageCreateRequest): Mono<MessageCreateResponse> {
        return request.message.toModel().let {
            messageService.create(it)
        }.flatMap {
            chatService.addMessage(it.id!!, request.chatId)
        }
            .then(createSuccessResponse().toMono())
            .onErrorResume { createFailureResponse(it).toMono() }
    }

    private fun createSuccessResponse(): MessageCreateResponse {
        return MessageCreateResponse.newBuilder().apply {
            successBuilder.apply {
                this.result = "Message created successfully"
            }
        }.build()
    }

    private fun createFailureResponse(e: Throwable): MessageCreateResponse {
        return MessageCreateResponse.newBuilder().apply {
            failureBuilder.apply {
                this.message = e.message
                this.errorBuilder
            }
        }.build()
    }
}
