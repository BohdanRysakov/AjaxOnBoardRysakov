package rys.ajaxpetproject.nats.controller

import io.nats.client.Message
import io.nats.client.MessageHandler
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

class ReactiveNatsHandler(
    private val natsController: NatsController<*, *>,
) : MessageHandler {

    override fun onMessage(message: Message) {
        natsController.handle(message)
            .map { it.toByteArray() }
            .doOnNext { natsController.connection.publish(message.replyTo, it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }
    companion object {
         val logger = org.slf4j.LoggerFactory.getLogger(ReactiveNatsHandler::class.java)
    }
}
fun main() {
    val list = mutableListOf<String>()
    Flux.range(0, 4)
        .log()
        .parallel() // This will turn the Flux into a ParallelFlux
        .doOnNext {
            System.err.println("${Thread.currentThread().name} In do on next $it + ${System.nanoTime()}")
            addition(list, "$it I")
            Thread.sleep(1000L)
            System.err.println("${Thread.currentThread().name} Out do on next $it + ${System.nanoTime()}")
            addition(list, "$it O")
        }
        .sequential() // Convert back to a regular Flux
        .map {
            println("${Thread.currentThread().name} Map with $it + ${System.nanoTime()}")
            it
        }
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe()

    Thread.sleep(60000L)
    println("List: $list")
}

fun addition(list : MutableList<String>, value: String) {
    Thread.sleep(10L* (Math.random()*100).toLong())
//    synchronized(list) {
        list.add(value)
//    }
}

