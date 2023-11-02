package rys.ajaxpetproject.service

import io.nats.client.Connection
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription
import rys.ajaxpetproject.utils.toProto

@Service
@Suppress("TooGenericExceptionCaught")
class MessageEventService(
    natsConnection: Connection, private val chatService: ChatService
) {

    private val messageParser = MessageDto.parser()

    private val natsDispatcher = natsConnection.createDispatcher()

    fun publishMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse> {
        return Flux.create { sink ->
            val subject = MessageEvent.createMessageCreateNatsSubject(chatId)

            natsDispatcher.apply {
                subscribe(subject) { msg ->
                    try {
                        val messageDto = messageParser.parseFrom(msg.data)
                        val response = buildSuccessResponse(messageDto)
                        sink.next(response)
                    } catch (e: Exception) {
                        sink.error(e)
                    }
                }
            }

            sink.onDispose {
                natsDispatcher.unsubscribe(subject)
            }
        }
    }

    fun loadInitialState(chatId: String): Flux<EventSubscription.CreateSubscriptionResponse> {
        return chatService.getMessagesInChat(chatId)
            .flatMap { message ->
                val messageDto = message.toDto(chatId)
                val response = buildSuccessResponse(messageDto)
                Flux.just(response)
            }
    }

    private fun buildSuccessResponse(messageDto: MessageDto):
            EventSubscription.CreateSubscriptionResponse {
        return EventSubscription.CreateSubscriptionResponse.newBuilder().apply {
            successBuilder.messageDto = messageDto
        }.build()
    }

    private fun MongoMessage.toDto(chatId: String): MessageDto {
        val message = this
        return MessageDto.newBuilder().apply {
            this.message = message.toProto()
            this.chatId = chatId
        }.build()
    }
}
