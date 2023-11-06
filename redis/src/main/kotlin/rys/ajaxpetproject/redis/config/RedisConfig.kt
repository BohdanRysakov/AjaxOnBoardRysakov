package rys.ajaxpetproject.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage

@Configuration
@EnableCaching
class RedisConfig
{
    @Bean
    fun  reactiveChatRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, MongoChat> {
        val objectMapper = ObjectMapper().findAndRegisterModules()
        val serializer = Jackson2JsonRedisSerializer(objectMapper, MongoChat::class.java)
        val context = RedisSerializationContext
            .newSerializationContext<String, MongoChat>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, context)
    }

    @Bean
    fun  reactiveMessageRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, MongoMessage> {
        val objectMapper = ObjectMapper().findAndRegisterModules()
        val serializer = Jackson2JsonRedisSerializer(objectMapper, MongoMessage::class.java)
        val context = RedisSerializationContext
            .newSerializationContext<String, MongoMessage>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(connectionFactory, context)
    }
}
