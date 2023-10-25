package rys.ajaxpetproject.nats.controller

import io.nats.client.Message
import io.nats.client.MessageHandler
import reactor.core.scheduler.Schedulers

class ReactiveNatsHandler(
    private val natsController: NatsController<*, *>,
) : MessageHandler {

    override fun onMessage(message: Message) {
        natsController.handle(message)
            .doOnNext {
                logger.info("Message {} sent to {}", it.toString(), message.replyTo)
            }
            .map { it.toByteArray() }
            .doOnNext { natsController.connection.publish(message.replyTo, it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }


    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ReactiveNatsHandler::class.java)
    }
}
