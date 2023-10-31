package rys.ajaxpetproject.kafka.configuration

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.kafka.receiver.ReceiverOptions
import rys.ajaxpetproject.subjects.KafkaTopic
import java.util.*


@EnableKafka
@Configuration
class KafkaConsumerConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var kafkaAddress: String

    @Bean
    fun kafkaReceiverOptions(): ReceiverOptions<String, ByteArray> {
        val basicReceiverOptions: ReceiverOptions<String, ByteArray> =
            ReceiverOptions.create(
                mapOf(
                    Pair(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress),
                    Pair(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java),
                    Pair(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProtobufDeserializer::class.java),
                    Pair(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                    Pair(ConsumerConfig.GROUP_ID_CONFIG, "ajax")
                )
            )
        return basicReceiverOptions.subscription(Collections.singletonList(KafkaTopic.MESSAGE_ADDED_TO_CHAT))
    }

    @Bean
    fun reactiveKafkaConsumerTemplate(
        kafkaReceiverOptions: ReceiverOptions<String, ByteArray>
    ): ReactiveKafkaConsumerTemplate<String, ByteArray> {
        return ReactiveKafkaConsumerTemplate(kafkaReceiverOptions)
    }
}

class ProtobufDeserializer : Deserializer<ByteArray> {

    override fun deserialize(topic: String, data: ByteArray): ByteArray {
        return data
    }
}
