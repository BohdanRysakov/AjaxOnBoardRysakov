package rys.ajaxpetproject.chat.infrastructure.nats

import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import rys.ajaxpetproject.repository.MessageRepository
import rys.ajaxpetproject.repository.impl.MessageRepositoryImpl
import rys.ajaxpetproject.service.impl.UserServiceImpl


@SpringBootConfiguration
@EnableAutoConfiguration(exclude = [GrpcServerFactoryAutoConfiguration::class])
@ComponentScan(
    value = [
        "rys.ajaxpetproject.repository",
        "rys.ajaxpetproject.service",
        "rys.ajaxpetproject.chat.infrastructure.nats",
        "rys.ajaxpetproject.chat.infrastructure.mongo",
        "rys.ajaxpetproject.chat.infrastructure.kafka",
        "rys.ajaxpetproject.chat.application.service",
        "rys.ajaxpetproject.chat.infrastructure.nats.config",
        "rys.ajaxpetproject.chat.infrastructure.nats.config",
    ])
@ContextConfiguration(
    classes = [
        UserServiceImpl::class,
        MessageRepository::class,
        MessageRepositoryImpl::class,
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

    @Bean
    fun getPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
