package rys.ajaxpetproject.chat.infrastructure.redis

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.switchIfEmptyDeferred
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.out.ChatServiceOutPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.internalapi.RedisPrefixes.CHAT_CACHE_KEY_PREFIX
import rys.ajaxpetproject.internalapi.RedisPrefixes.MESSAGE_CACHE_KEY_PREFIX

@Repository
@Primary
@Suppress("TooManyFunctions")
class CacheChatRepository(
    @Qualifier("chatRepository") private val actualRepository: ChatServiceOutPort,
    private val redisOperations: ReactiveRedisTemplate<String, Chat>
) : ChatServiceOutPort by actualRepository {
    override fun findChatById(id: String): Mono<Chat> {
        return redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$id")
            .switchIfEmpty { findAndCacheChat(id) }
    }

    override fun save(chat: Chat): Mono<Chat> {
        return actualRepository.save(chat)
            .flatMap { savedChat ->
                redisOperations.opsForValue().set("$CHAT_CACHE_KEY_PREFIX${savedChat.id!!}", savedChat)
                    .thenReturn(savedChat)
            }
            .doOnSuccess { savedChat ->
                logger.info("Chat with id {} was saved in cache", savedChat.id)
            }
    }

    override fun deleteAll(): Mono<Unit> {
        return actualRepository.deleteAll()
            .thenMany(
                redisOperations.scan(ScanOptions.scanOptions().match("$CHAT_CACHE_KEY_PREFIX*").build())
            )
            .flatMap { msg ->
                redisOperations.opsForValue().delete(msg)
            }
            .then(Unit.toMono())
            .doOnSuccess { logger.info("All chats were deleted from cache") }
    }

    override fun update(id: String, chat: Chat): Mono<Chat> {
        return actualRepository.update(id, chat)
            .flatMap { updatedChat ->
                redisOperations.opsForValue().set("$CHAT_CACHE_KEY_PREFIX$id", updatedChat)
                    .thenReturn(updatedChat)
            }
            .doOnSuccess { logger.info("Chat with id {} was updated in cache", id) }
    }

    override fun addUser(userId: String, chatId: String): Mono<Unit> {
        return actualRepository.addUser(userId, chatId)
            .then(findAndCacheChat(chatId))
            .thenReturn(Unit)
            .doOnSuccess { logger.info("User with id {} was added to chat with id {} in cache", userId, chatId) }
    }

    override fun removeUser(userId: String, chatId: String): Mono<Unit> {
        return actualRepository.removeUser(userId, chatId)
            .then(findAndCacheChat(chatId))
            .thenReturn(Unit)
            .doOnSuccess { logger.info("User with id {} was removed from chat with id {} in cache", userId, chatId) }
    }

    override fun addMessage(messageId: String, chatId: String): Mono<Unit> {
        return actualRepository.addMessage(messageId, chatId)
            .then(findAndCacheChat(chatId))
            .thenReturn(Unit)
            .doOnSuccess {
                logger.info("Message with id {} was added to chat with id {} in cache", messageId, chatId)
            }


    }

    override fun removeMessage(messageId: String, chatId: String): Mono<Unit> {
        return actualRepository.removeMessage(messageId, chatId)
            .then(findAndCacheChat(chatId))
            .thenReturn(Unit)
            .doOnSuccess {
                logger.info("Message with id {} was removed from chat with id {} in cache", messageId, chatId)
            }
    }

    override fun removeMessages(ids: List<String>, chatId: String): Mono<Unit> {
        return actualRepository.removeMessages(ids, chatId)
            .then(findAndCacheChat(chatId))
            .thenReturn(Unit)
            .doOnSuccess {
                logger.info("Messages with ids {} were removed from chat with id {} in cache", ids, chatId)
            }

    }

    override fun delete(id: String): Mono<Unit> {
        return actualRepository.delete(id)
            .then(redisOperations.opsForValue().delete("$CHAT_CACHE_KEY_PREFIX$id"))
            .then(Unit.toMono())
            .doOnSuccess { logger.info("Chat with id {} was removed from cache", id) }
    }

    override fun findAll(): Flux<Chat> {
        return redisOperations.scan(
            ScanOptions.scanOptions()
                .match("$CHAT_CACHE_KEY_PREFIX*").build()
        )
            .flatMap { chatId ->
                redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$chatId")
            }
            .switchIfEmptyDeferred {
                actualRepository.findAll()
            }
    }

    override fun deleteMessagesFromChatByUserId(chatId: String, userId: String): Mono<Unit> {
        return findMessagesByUserIdAndChatId(userId, chatId)
            .flatMap { message ->
                redisOperations.opsForValue().delete("$MESSAGE_CACHE_KEY_PREFIX${message.id}")
            }
            .then(actualRepository.deleteMessagesFromChatByUserId(chatId, userId))
            .then(findAndCacheChat(chatId))
            .thenReturn(Unit)
            .doOnSuccess { logger.info("Messages from chat with id {} were removed from cache", chatId) }
    }

    private fun findAndCacheChat(chatId: String): Mono<Chat> {
        return actualRepository.findChatById(chatId)
            .flatMap { foundChat ->
                redisOperations.opsForValue().set("$CHAT_CACHE_KEY_PREFIX$chatId", foundChat)
                    .thenReturn(foundChat)
                    .doOnSuccess {
                        logger.info("Chat with id {} was saved in cache", chatId)
                    }
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CacheChatRepository::class.java)
    }
}
