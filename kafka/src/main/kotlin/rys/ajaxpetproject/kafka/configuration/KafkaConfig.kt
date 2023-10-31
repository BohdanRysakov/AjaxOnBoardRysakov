package rys.ajaxpetproject.kafka.configuration

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent
import rys.ajaxpetproject.subjects.KafkaTopic
import java.util.Collections

@Configuration
@EnableKafka
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String
) {
    @Bean
    fun kafkaSenderEvent(): KafkaSender<String, MessageCreateEvent> =
        createKafkaSender(producerProperties())

    private fun createKafkaSender(properties: MutableMap<String, Any>):
            KafkaSender<String, MessageCreateEvent> =
        KafkaSender.create(SenderOptions.create(properties))

    private fun producerProperties(customProperties: MutableMap<String, Any> = mutableMapOf()):
            MutableMap<String, Any> {
        val baseProperties: MutableMap<String, Any> = mutableMapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaProtobufSerializer::class.java.name,
            "schema.registry.url" to schemaRegistryUrl
        )
        baseProperties.putAll(customProperties)
        return baseProperties
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, MessageCreateEvent> =
        KafkaReceiver.create(receiverOptions())

    private fun  receiverOptions(): ReceiverOptions<String, MessageCreateEvent> {
        return ReceiverOptions.create<String, MessageCreateEvent>(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.CLIENT_ID_CONFIG to "my-client-id",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaProtobufDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to "my-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                "schema.registry.url" to schemaRegistryUrl
            )
        ).subscription(Collections.singleton(KafkaTopic.MESSAGE_ADDED_TO_CHAT))
    }
}
