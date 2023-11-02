package rys.ajaxpetproject.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreatedEvent
import rys.ajaxpetproject.internalapi.MessageEvent

@Component
class MessageCreateEventProducer(
    private val kafkaSender: KafkaSender<String, MessageCreatedEvent>
) {
    fun sendCreateEvent(event: MessageCreatedEvent): Mono<Unit> {

        val senderRecord = SenderRecord.create(
            ProducerRecord(
                MessageEvent.MESSAGE_CREATE_EVENT,
                event.chatId,
                event
            ),
            null
        )
        return kafkaSender.send(senderRecord.toMono())
            .then(Unit.toMono())
    }
}
