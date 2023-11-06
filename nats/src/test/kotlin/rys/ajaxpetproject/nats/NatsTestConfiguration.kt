package rys.ajaxpetproject.nats

import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootConfiguration
@EnableMongoRepositories(basePackages = ["rys.ajaxpetproject.repository"])
@ActiveProfiles("tests")
@EnableAutoConfiguration
@SpringBootTest
@ContextConfiguration(
    classes = [
        MongoRepositoriesAutoConfiguration::class,
    ]
)
class NatsTestConfiguration {
    @Value("\${nats.uri}")
    private lateinit var natsUri: String

    @Bean
    @ConditionalOnMissingBean
    fun connection(): Connection {
        val options = Options.Builder()
            .server(natsUri)
            .build()
        return Nats.connect(options)
    }

}
