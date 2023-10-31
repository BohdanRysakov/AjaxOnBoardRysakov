package rys.ajaxpetproject.kafka

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.kafka.receiver.KafkaReceiver
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent

@Component
class MessageCreateEventReceiver(
    private val kafkaReceiver: KafkaReceiver<String, MessageCreateEvent>
) {

    @PostConstruct
    fun start() {
        kafkaReceiver.receive()
            .doOnNext { record ->
                val key = record.key()
                val messageCreateEvent = record.value()
                handleEvent(key, messageCreateEvent)
            }
            .subscribe()
    }

    private fun handleEvent(chatId: String, event: MessageCreateEvent) {
        // Handle the received event
        logger.info("Received message for chatId $chatId: ${event.message}")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageCreateEventReceiver::class.java)
    }
}
