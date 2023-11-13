package rys.ajaxpetproject.chat.infrastructure.nats.event

import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import rys.ajaxpetproject.chat.application.port.out.EventListenerOutPort
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription


@Component
@Suppress("TooGenericExceptionCaught")
class EventNatsSubscriber(
    natsConnection: Connection,
) : EventListenerOutPort {

    private val messageParser = MessageDto.parser()

    private val natsDispatcher = natsConnection.createDispatcher()

    override fun catchMessageCreatedEvent(chatId: String):
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


    private fun buildSuccessResponse(messageDto: MessageDto):
            EventSubscription.CreateSubscriptionResponse {
        return EventSubscription.CreateSubscriptionResponse.newBuilder().apply {
            successBuilder.messageDto = messageDto
        }.build()
    }
}
