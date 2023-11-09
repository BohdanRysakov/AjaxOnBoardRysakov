package rys.ajaxpetproject.chat.infractructure.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.`in`.IMessageAddEventInPort
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

@Component
class MessageAddEventProducer(
    private val kafkaSender: KafkaSender<String, CreateEvent.MessageCreatedEvent>
) : IMessageAddEventInPort {
    override fun sendCreateEvent(event: CreateEvent.MessageCreatedEvent): Mono<Unit> {

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
