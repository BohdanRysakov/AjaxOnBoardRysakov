package rys.ajaxpetproject.chat.infractructure.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.out.IChatEventPublisherOutPort
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreatedEvent
import rys.ajaxpetproject.internalapi.MessageEvent

@Component
class MessageCreateEventProducer(
    private val kafkaSender: KafkaSender<String, MessageCreatedEvent>
) : IChatEventPublisherOutPort {
    override fun sendEvent(event: MessageCreatedEvent): Mono<Unit> {

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
