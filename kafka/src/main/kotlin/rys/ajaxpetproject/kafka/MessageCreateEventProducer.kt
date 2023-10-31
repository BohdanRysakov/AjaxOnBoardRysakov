package rys.ajaxpetproject.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent
import rys.ajaxpetproject.subjects.KafkaTopic
import rys.ajaxpetproject.utils.toProto

@Component
class MessageCreateEventProducer(
    private val kafkaSender: KafkaSender<String, MessageCreateEvent>
) {
    fun sendCreateEvent(event: MessageCreateEvent) {
        val senderRecord = SenderRecord.create(
            ProducerRecord(
                KafkaTopic.MESSAGE_ADDED_TO_CHAT,
                event.chatId,
                event
            ),
            null
        )
        kafkaSender.send(senderRecord.toMono()).subscribe()
    }
}
