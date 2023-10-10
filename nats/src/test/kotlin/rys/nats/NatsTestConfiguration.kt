package rys.nats

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import rys.nats.exception.InternalException
import rys.rest.model.MongoChat
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService
import rys.rest.service.impl.ChatServiceImpl


@SpringBootConfiguration
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

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create("mongodb://localhost:27018/testDB")
    }

    @Bean
    fun mongoTemplate(@Autowired mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, "testDB")
    }


    fun chatService(chatRepository: ChatRepository): ChatService {
        if(System.getProperty("mockChatService") != null) {
            val mock = Mockito.mock(ChatServiceImpl::class.java)
            whenever(mock.createChat(any<MongoChat>())).thenThrow(InternalException("Test exception"))
            return mock
        }
        return ChatServiceImpl(chatRepository)
    }

}
