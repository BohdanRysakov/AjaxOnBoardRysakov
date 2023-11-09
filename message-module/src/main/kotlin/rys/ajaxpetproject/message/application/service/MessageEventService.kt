package rys.ajaxpetproject.message.application.service

import io.nats.client.Connection
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.message.application.port.`in`.IEventSubInPort
import rys.ajaxpetproject.message.application.port.`in`.IMessageAddedEventPublishInPort
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.message.domain.entity.Message
import rys.ajaxpetproject.message.domain.entity.toProto
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription


@Suppress("TooGenericExceptionCaught")
@Service
class MessageEventService(
    natsConnection: Connection,
    private val chatService: IEventSubInPort
) : IMessageAddedEventPublishInPort {

    private val messageParser = MessageDto.parser()

    private val natsDispatcher = natsConnection.createDispatcher()

    override fun publishMessageCreatedEvent(chatId: String):
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

    override fun loadInitialState(chatId: String): Flux<EventSubscription.CreateSubscriptionResponse> {
        return chatService.getMessagesInChat(chatId)
            .flatMap { message ->
                val messageDto = message.toDto(chatId)
                val response = buildSuccessResponse(messageDto)
                response.toMono()
            }
    }

    private fun buildSuccessResponse(messageDto: MessageDto):
            EventSubscription.CreateSubscriptionResponse {
        return EventSubscription.CreateSubscriptionResponse.newBuilder().apply {
            successBuilder.messageDto = messageDto
        }.build()
    }

    private fun Message.toDto(chatId: String): MessageDto {
        val message = this
        return MessageDto.newBuilder().apply {
            this.chatId = chatId
            this.message = message.toProto()
        }
            .build()
    }
}
