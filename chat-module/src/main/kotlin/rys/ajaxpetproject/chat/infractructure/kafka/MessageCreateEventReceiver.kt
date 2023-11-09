package rys.ajaxpetproject.chat.infractructure.kafka

import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreatedEvent
import javax.annotation.PostConstruct

@Component
class MessageCreateEventReceiver(
    private val natsConnection: Connection,
    private val kafkaReceiver: KafkaReceiver<String, MessageCreatedEvent>
) {

    @PostConstruct
    fun startListening() {
        logger.info("Starting to listen to kafka")
        kafkaReceiver.receiveAutoAck()
            .flatMap { consumerRecord ->
                consumerRecord.map {
                    handleEvent(it.value())
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun handleEvent(event: MessageCreatedEvent) {
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
        private val logger = LoggerFactory.getLogger(MessageCreateEventReceiver::class.java)
    }
}

