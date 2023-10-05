package rys.nats.natsservice

import io.nats.client.Connection
import io.nats.client.Dispatcher
import io.nats.client.Subscription
import jakarta.annotation.PostConstruct
import jdk.incubator.vector.VectorOperators.LOG
import org.springframework.stereotype.Service
import rys.rest.repository.ChatRepository


@Service
class NatsListenerService(private val natsConnector: Connection, private val chatRepository: ChatRepository)
