package rys.ajaxpetproject.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent
import rys.ajaxpetproject.internalapi.MessageEvent

@Component
class MessageCreateEventProducer(
    private val kafkaSender: KafkaSender<String, MessageCreateEvent>
) {
    fun sendCreateEvent(event: MessageCreateEvent) {

        val senderRecord = SenderRecord.create(
            ProducerRecord(
                MessageEvent.MESSAGE_CREATE_EVENT,
                event.chatId,
                event
            ),
            null
        )
        kafkaSender.send(senderRecord.toMono()).subscribe()
    }
}
