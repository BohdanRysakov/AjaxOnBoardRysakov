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
    private val kafkaReceiver: KafkaReceiver<String, ByteArray>
) : CommandLineRunner{

    private val parser = MessageCreateEvent.parser()

    override fun run(vararg args: String?) {
        kafkaReceiver.receive()
            .log()
            .doOnNext{
                handleEvent(it.key(), it.value())
            }.subscribe({ logger.error("Actual info: ${parser.parseFrom(it.value())}")})
    }

    fun listen(): Flux<ReceiverRecord<String, ByteArray>> {
        return kafkaReceiver.receive()
            .log()
            .doOnNext{
                handleEvent(it.key(), it.value())
            }

    }

    private fun handleEvent(chatId: String, event: ByteArray) {
        // Handle the received event
        logger.error("Received message for chatId $chatId: ")
        logger.error("Actual info: ${parser.parseFrom(event)}")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageCreateEventReceiver::class.java)
    }
}
