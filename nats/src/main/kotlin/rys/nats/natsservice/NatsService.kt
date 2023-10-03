package rys.nats.natsservice

import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Subscription
import org.springframework.stereotype.Service

@Service
class NatsService(private val natsConnection: Connection) {

    fun publish(subject: String, message: String) {
        natsConnection.publish(subject, message.toByteArray())
    }

    fun subscribe(subject: String): Subscription {
        return natsConnection.subscribe(subject) { msg: Message ->
            println(String(msg.data))
        }
    }
}
