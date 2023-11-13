package rys.ajaxpetproject.chat.infrastructure.kafka

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import rys.ajaxpetproject.chat.infrastructure.nats.event.EventNatsPublisher
import rys.ajaxpetproject.request.message.create.proto.CreateEvent
import javax.annotation.PostConstruct

@Component
class MessageAddedEventReceiver(
    private val eventPublisher: EventNatsPublisher,
    private val kafkaReceiver: KafkaReceiver<String, CreateEvent.MessageCreatedEvent>
) {
    @PostConstruct
    fun startListening() {
        logger.info("Starting to listen to kafka")
        kafkaReceiver.receiveAutoAck()
            .flatMap { consumerRecord ->
                consumerRecord.map {
                    eventPublisher.handleEvent(it.value())
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }


    companion object {
        private val logger = LoggerFactory.getLogger(MessageAddedEventReceiver::class.java)
    }
}
