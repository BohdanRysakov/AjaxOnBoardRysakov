package rys.ajaxpetproject.grpc

import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.kafka.MessageCreateEventProducer
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.request.message.create.proto.CreateEvent
import rys.ajaxpetproject.request.message.create.proto.MessageCreateResponse
import rys.ajaxpetproject.request.message.create.proto.MessageCreateRequest
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.message.ReactorMessageServiceGrpc
import rys.ajaxpetproject.utils.toModel
import rys.ajaxpetproject.utils.toProto

@GrpcService
class MessageGrpc(
    private val chatService: ChatService,
    private val messageService: MessageService,
    private val kafka: MessageCreateEventProducer
) :
    ReactorMessageServiceGrpc.MessageServiceImplBase() {
    override fun create(request: MessageCreateRequest): Mono<MessageCreateResponse> {
        return request.message.toModel().let {
            messageService.create(it)
        }.map {
            chatService.addMessage(it.id!!, request.chatId)
        }
            .doOnNext { kafka.sendCreateEvent(createEvent(Pair(request.message.toModel(), request.chatId))) }
            .then(createSuccessResponse().toMono())
            .onErrorResume { createFailureResponse(it).toMono() }

    }

    private fun createEvent(eventData : Pair<MongoMessage,String>) : CreateEvent.MessageCreateEvent {
        return CreateEvent.MessageCreateEvent.newBuilder().apply {
            this.message = eventData.first.toProto()
            this.chatId = eventData.second
        }.build()
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

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(MessageGrpc::class.java)
    }
}
