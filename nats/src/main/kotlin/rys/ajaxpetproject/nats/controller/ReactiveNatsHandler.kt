package rys.ajaxpetproject.nats.controller

import io.nats.client.Message
import io.nats.client.MessageHandler
import org.slf4j.LoggerFactory
import reactor.core.scheduler.Schedulers

class ReactiveNatsHandler(
    private val natsChatController: NatsController<*, *>,
) : MessageHandler {

    override fun onMessage(message: Message) {
        natsChatController.handle(message)
            .doOnNext {
                logger.info("Received message: subject={}, message={}", message.replyTo, it)
            }
            .map { it.toByteArray() }
            .doOnNext { natsChatController.connection.publish(message.replyTo, it) }
            .doOnError { logger.error("Error while handling message: {}", it.message, it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReactiveNatsHandler::class.java)
    }
}
