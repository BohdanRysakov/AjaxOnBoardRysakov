package rys.ajaxpetproject.kafka

import io.nats.client.Connection
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import reactor.kafka.receiver.KafkaReceiver
import rys.ajaxpetproject.internalapi.MessageEvent
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent

@Component
class MessageCreateEventReceiver(
    private val natsConnection : Connection,
    private val kafkaReceiver: KafkaReceiver<String, MessageCreateEvent>
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        kafkaReceiver.receive()
            .doOnNext {
                handleEvent(it.value())
            }.subscribe()
    }

    private fun handleEvent(event: MessageCreateEvent) {

        natsConnection.publish(
            MessageEvent.createMessageCreateNatsSubject(event.chatId),
            event.toByteArray())

        logger.error("Published message in " +
                "${MessageEvent.createMessageCreateNatsSubject(event.chatId)} " +
                " - [${event.message.content}]")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageCreateEventReceiver::class.java)
    }
}

