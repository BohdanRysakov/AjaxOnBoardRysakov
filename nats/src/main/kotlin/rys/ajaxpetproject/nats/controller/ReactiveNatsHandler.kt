package rys.ajaxpetproject.nats.controller

import io.nats.client.Message
import io.nats.client.MessageHandler
import org.slf4j.LoggerFactory
import reactor.core.scheduler.Schedulers

class ReactiveNatsHandler(
    private val natsController: NatsController<*, *>,
) : MessageHandler {

    override fun onMessage(message: Message) {
        natsController.handle(message)
            .doOnNext {
                logger.info("Received message: subject={}, message={}", message.replyTo, it)
            }
            .map { it.toByteArray() }
            .doOnNext { natsController.connection.publish(message.replyTo, it) }
            .doOnError{ logger.error("Error while handling message: {}", it.message, it)}
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReactiveNatsHandler::class.java)
    }
}
