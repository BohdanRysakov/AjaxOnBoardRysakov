package rys.nats

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootConfiguration
@EnableMongoRepositories(basePackages = ["rys.rest.repository"])
class NatsTestConfiguration {
    @Value("\${nats.uri}")
    private lateinit var natsUri: String

    @Value("\${spring.data.mongodb.uri}")
    private lateinit var mongoUri: String

    @Bean
    @ConditionalOnMissingBean
    fun connection(): Connection {
        val options = Options.Builder()
            .server(natsUri)
            .build()
        return Nats.connect(options)
    }

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    fun mongoTemplate(@Autowired mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, "testDB")
    }
}
