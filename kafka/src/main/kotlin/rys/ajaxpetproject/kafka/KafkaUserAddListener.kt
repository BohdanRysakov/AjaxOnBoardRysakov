package rys.ajaxpetproject.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.core.publisher.Flux
import rys.ajaxpetproject.subjects.KafkaTopic

class KafkaUserAddListener(
    val reactiveKafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, ByteArray>
) : CommandLineRunner {

    @KafkaListener(topics = [KafkaTopic.MESSAGE_ADDED_TO_CHAT])
    private fun listen(): Flux<ByteArray> {
        return reactiveKafkaConsumerTemplate
            .receiveAutoAck()
            .map { it.value() }
            .doOnNext {
                logger.info("[KAFKA] Received message: ${it.toString(Charsets.UTF_8)}")
            }
    }
    override fun run(vararg args: String?) {
        listen().subscribe()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaUserAddListener::class.java)
    }
}
