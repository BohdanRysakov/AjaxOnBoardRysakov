package rys.ajaxpetproject.redis.repository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.internalapi.RedisPrefixes.MESSAGE_CACHE_KEY_PREFIX
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository

@Repository
@Primary
class CacheMessageRepository(
    @Qualifier("messageRepositoryImpl") private val actualRepository: MessageRepository,
    private val redisOperations: ReactiveRedisOperations<String, MongoMessage>
) : MessageRepository by actualRepository {

    override fun findMessageById(id: String): Mono<MongoMessage> {
        return redisOperations.opsForValue().get("$MESSAGE_CACHE_KEY_PREFIX$id")
            .switchIfEmpty { findAndCacheMessage(id) }
    }

    override fun save(message: MongoMessage): Mono<MongoMessage> {
        return actualRepository.save(message)
            .flatMap {
                redisOperations.opsForValue().set("$MESSAGE_CACHE_KEY_PREFIX${it.id!!}", it)
                    .thenReturn(it)
            }
            .doOnSuccess { savedMessage ->
                logger.info("Message with id {} was saved in cache", savedMessage.id)
            }
    }

    override fun deleteAll(): Mono<Unit> {
        return actualRepository.deleteAll()
            .thenMany(
                redisOperations.scan(ScanOptions.scanOptions().match("$MESSAGE_CACHE_KEY_PREFIX*").build())
            )
            .flatMap {
                redisOperations.opsForValue().delete(it)
            }
            .then(Unit.toMono())
            .doOnSuccess { logger.info("All messages were deleted from cache") }
    }

    override fun update(id: String, message: MongoMessage): Mono<MongoMessage> {
        return actualRepository.update(id, message)
            .then(findAndCacheMessage(id))
            .doOnSuccess { logger.info("Message with id {} was updated in cache", id) }
    }

    override fun delete(id: String): Mono<Unit> {
        return actualRepository.delete(id)
            .then(redisOperations.opsForValue().delete("$MESSAGE_CACHE_KEY_PREFIX$id"))
            .doOnSuccess { logger.info("Message with id {} was deleted from cache", id) }
            .thenReturn(Unit)
    }

    override fun deleteMessagesByIds(ids: List<String>): Mono<Unit> {
        return actualRepository.deleteMessagesByIds(ids)
            .then(redisOperations.delete(Flux.fromIterable(ids).map { "$MESSAGE_CACHE_KEY_PREFIX$it" }))
            .thenReturn(Unit)
            .doOnSuccess { logger.info("Messages with ids {} were deleted from cache", ids) }
    }

    private fun findAndCacheMessage(messageId: String): Mono<MongoMessage> {
        return actualRepository.findMessageById(messageId)
            .flatMap { savedMessage ->
                redisOperations.opsForValue().set("$MESSAGE_CACHE_KEY_PREFIX$messageId", savedMessage)
                    .thenReturn(savedMessage)
            }
            .doOnSuccess { logger.info("Message with id {} was found in cache", messageId) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CacheMessageRepository::class.java)
    }
}
