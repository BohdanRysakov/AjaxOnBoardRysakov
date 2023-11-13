package rys.ajaxpetproject.chat.infrastructure.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message

@Configuration
@EnableCaching
class ChatDomainRedisConfig {
    @Bean
    fun reactiveChatRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, Chat> {
        val objectMapper = ObjectMapper().findAndRegisterModules()
        val serializer = Jackson2JsonRedisSerializer(objectMapper, Chat::class.java)
        val context = RedisSerializationContext
            .newSerializationContext<String, Chat>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, context)
    }

    @Bean
    fun reactiveMessageRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, Message> {
        val objectMapper = ObjectMapper().findAndRegisterModules()
        val serializer = Jackson2JsonRedisSerializer(objectMapper, Message::class.java)
        val context = RedisSerializationContext
            .newSerializationContext<String, Message>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, context)
    }
}
