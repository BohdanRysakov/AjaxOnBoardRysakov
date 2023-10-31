package rys.ajaxpetproject.kafka

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.kafka.receiver.KafkaReceiver

@Component
class KafkaListener(private val kafkaReceiver: KafkaReceiver<String, String>) {
    fun listen() {
        kafkaReceiver.receive()
            .doOnNext{
                logger.error("Received message: ${it.value()}")
            }
            .subscribe()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaListener::class.java)
    }
}
