package rys.ajaxpetproject.kafka.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String)
{

    @Bean
    fun topic() : NewTopic = NewTopic("my-topic", 1, 1)

    @Bean
    fun kafkaSenderEvent(): KafkaSender<String, String> =
        createKafkaSender(producerProperties())

    private fun  createKafkaSender(properties: MutableMap<String, Any>):
            KafkaSender<String, String> =
        KafkaSender.create(SenderOptions.create(properties))

    private fun producerProperties(customProperties: MutableMap<String, Any> = mutableMapOf()):
            MutableMap<String, Any> {
        val baseProperties: MutableMap<String, Any> = mutableMapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        )
        baseProperties.putAll(customProperties)
        return baseProperties
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, String> =
        KafkaReceiver.create(receiverOptions())

    private fun receiverOptions(): ReceiverOptions<String, String> {
        return ReceiverOptions.create<String, String>(mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.GROUP_ID_CONFIG to "my-group",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )).subscription(setOf("my-topic"))
    }
}
