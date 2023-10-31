package rys.ajaxpetproject.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent

@Component
class MessageCreateEventReceiver(
    private val kafkaReceiver: KafkaReceiver<String, MessageCreateEvent>
) : CommandLineRunner{

    override fun run(vararg args: String?) {
        kafkaReceiver.receive()
            .log()
            .doOnNext{
                handleEvent(it.key(), it.value())
            }.subscribe({ logger.error(it.value().chatId.toString())}, {logger.error(it.message)})
    }

    fun listen(): Flux<ReceiverRecord<String, MessageCreateEvent>> {
        return kafkaReceiver.receive()
            .log()
            .doOnNext{
                handleEvent(it.key(), it.value())
            }

    }

    private fun handleEvent(chatId: String, event: MessageCreateEvent) {
        // Handle the received event
        logger.error("Received message for chatId $chatId: ")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageCreateEventReceiver::class.java)
    }
}
