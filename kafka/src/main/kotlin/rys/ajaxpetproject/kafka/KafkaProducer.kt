package rys.ajaxpetproject.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono


@Component
class KafkaProducer(
    private val kafkaSender: KafkaSender<String, String>
) {
    fun sendMessage(topic: String, key: String?, message: String) {
        val senderRecord = SenderRecord.create(ProducerRecord(topic, key, message),
            null)
          kafkaSender.send(senderRecord.toMono()).onErrorResume { IllegalArgumentException("!!").toMono() }.subscribe()
    }

}
