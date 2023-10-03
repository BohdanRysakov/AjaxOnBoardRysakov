package rys.nats.config


import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfig {

    private val uri = "nats://localhost:4222"
    private val log = LoggerFactory.getLogger(NatsConfig::class.java)

    @Bean
    fun natsConnection(): Connection {
        val options: Options = Options.Builder()
            .errorCb { ex -> log.error("Connection Exception: ", ex) }
            .disconnectedCb { event -> log.error("Channel disconnected: {}", event.connection) }
            .reconnectedCb { event -> log.error("Reconnected to server: {}", event.connection) }
            .build()
        return Nats.connect(uri, options)
    }
}
