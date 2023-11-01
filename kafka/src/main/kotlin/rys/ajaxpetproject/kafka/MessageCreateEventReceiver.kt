package rys.ajaxpetproject.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import reactor.kafka.receiver.KafkaReceiver
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent

@Component
class MessageCreateEventReceiver(
    private val kafkaReceiver: KafkaReceiver<String, MessageCreateEvent>
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        kafkaReceiver.receive().log()
            .doOnNext {
                handleEvent(it.key(), it.value())
            }.subscribe({ logger.error(it.value().chatId.toString()) }, { logger.error(it.message) })
    }

    private fun handleEvent(chatId: String, event: MessageCreateEvent) {
        // Handle the received event
        logger.info("Received message for chatId $chatId: ${event.message}")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageCreateEventReceiver::class.java)
    }
}

