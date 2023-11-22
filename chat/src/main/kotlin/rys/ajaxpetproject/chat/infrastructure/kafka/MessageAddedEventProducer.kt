package rys.ajaxpetproject.chat.infrastructure.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.output.EventSenderOutPort
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent
import rys.ajaxpetproject.chat.infrastructure.kafka.mapper.toProto
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

@Component
class MessageAddedEventProducer(
    private val kafkaSender: KafkaSender<String, CreateEvent.MessageCreatedEvent>
) : EventSenderOutPort {
    override fun sendCreateEvent(event: MessageAddedEvent): Mono<Unit> {

        val protoEvent = event.toProto()

        val senderRecord = SenderRecord.create(
            ProducerRecord(
                MessageEvent.MESSAGE_CREATE_EVENT,
                protoEvent.chatId,
                protoEvent
            ),
            null
        )
        return kafkaSender.send(senderRecord.toMono())
            .then(Unit.toMono())
    }
}
