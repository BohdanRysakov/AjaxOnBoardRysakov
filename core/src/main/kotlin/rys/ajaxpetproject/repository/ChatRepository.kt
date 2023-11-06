package rys.ajaxpetproject.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage

@Suppress("TooManyFunctions")
interface ChatRepository {
    fun findChatById(id: String): Mono<MongoChat>

    fun save(chat: MongoChat): Mono<MongoChat>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, chat: MongoChat): Mono<MongoChat>

    fun addUser(userId: String, chatId: String): Mono<Unit>

    fun removeUser(userId: String, chatId: String): Mono<Unit>

    fun addMessage(messageId: String, chatId: String): Mono<Unit>

    fun removeMessage(messageId: String, chatId: String): Mono<Unit>

    fun delete(id: String): Mono<Unit>

    fun findAll(): Flux<MongoChat>

    fun findChatsByUserId(userId: String): Flux<MongoChat>

    fun findMessagesByUserIdAndChatId(userId: String, chatId: String): Flux<MongoMessage>

    fun findMessagesFromChat(chatId: String) : Flux<MongoMessage>

    fun deleteMessagesFromChatByUserId(chatId: String, userId: String): Mono<Unit>
}
