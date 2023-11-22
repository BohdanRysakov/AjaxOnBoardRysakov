package rys.ajaxpetproject.chat.infrastructure.nats.event

import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent
import rys.ajaxpetproject.chat.infrastructure.adapter.EventListener
import rys.ajaxpetproject.chat.infrastructure.nats.mapper.toDomainMessage
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.internalapi.MessageEvent

@Component
@Suppress("TooGenericExceptionCaught")
class EventNatsSubscriber(
    natsConnection: Connection,
) : EventListener {

    private val messageParser = MessageDto.parser()

    private val natsDispatcher = natsConnection.createDispatcher()

    override fun handleMessageCreatedEvent(chatId: String):
            Flux<MessageAddedEvent> {
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
            MessageAddedEvent {
        return MessageAddedEvent(
            chatId = messageDto.chatId,
            message = messageDto.message.toDomainMessage()
        )
    }
}
