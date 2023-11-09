package rys.ajaxpetproject.chat.infractructure.kafka

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import rys.ajaxpetproject.chat.infractructure.nats.EventNatsPublisher
import rys.ajaxpetproject.request.message.create.proto.CreateEvent
import javax.annotation.PostConstruct

@Component
class MessageAddEventReceiver(
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
        private val logger = LoggerFactory.getLogger(MessageAddEventReceiver::class.java)
    }
}
