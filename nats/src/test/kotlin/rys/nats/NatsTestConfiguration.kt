package rys.nats

import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories


@TestConfiguration
@EnableMongoRepositories(basePackages = ["rys.rest.repository"])
class NatsTestConfiguration {
    private val uri = "nats://localhost:4222"

    @Bean
    @ConditionalOnMissingBean
    fun connection(): Connection {
        val options = Options.Builder()
            .server(uri)
            .build()
        val connection = Nats.connect(options)
        return connection
    }
}
