package rys.nats.config

import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfig {
    @Value("\${nats.uri}")
    private lateinit var natsUri: String

    private val log = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun connection(): Connection {
        val options = Options.Builder()
            .server(natsUri)
            .build()
        val connection = Nats.connect(options)

        log.info("Connected to NATS server at $natsUri")

        return connection
    }
}
