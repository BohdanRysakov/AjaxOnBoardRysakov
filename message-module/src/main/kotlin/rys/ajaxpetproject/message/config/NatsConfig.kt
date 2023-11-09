package rys.ajaxpetproject.message.config

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

    @Bean
    fun connection(): Connection {
        val options = Options.Builder()
            .server(natsUri)
            .build()
        val connection = Nats.connect(options)

        logger.info("Connected to NATS server at {}", natsUri)

        return connection
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NatsConfig::class.java)
    }
}
