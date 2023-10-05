package rys.nats.config


import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfig {

    private val uri = "nats://localhost:4222"
    private val log = LoggerFactory.getLogger(NatsConfig::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun connection(): Connection {
        val options = Options.Builder()
            .server(uri)
            .build()
        val connection = Nats.connect(options)
        log.info("Connected to NATS server at $uri")
        return connection
    }

}
