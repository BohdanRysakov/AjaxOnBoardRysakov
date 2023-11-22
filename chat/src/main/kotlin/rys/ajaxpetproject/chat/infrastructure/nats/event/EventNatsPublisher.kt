package rys.ajaxpetproject.chat.infrastructure.nats.event

import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import rys.ajaxpetproject.chat.infrastructure.adapter.EventBroadcaster
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

@Component
class EventNatsPublisher(
    private val natsConnection: Connection
) : EventBroadcaster {
    override fun handleEvent(event: CreateEvent.MessageCreatedEvent) {
        if (event.chatId.isNotBlank()) {
            natsConnection.publish(
                MessageEvent.createMessageCreateNatsSubject(event.chatId),
                MessageDto.newBuilder().apply {
                    this.chatId = event.chatId
                    this.message = event.message
                }.build().toByteArray()
            )
            logger.info(
                "Published message in {} - {}",
                MessageEvent.createMessageCreateNatsSubject(event.chatId),
                event.message
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EventNatsPublisher::class.java)
    }
}
