package rys.ajaxpetproject.chat.application.port.output

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message

@Suppress("TooManyFunctions")
interface ChatServiceOutPort {
    fun findChatById(id: String): Mono<Chat>

    fun save(chat: Chat): Mono<Chat>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, chat: Chat): Mono<Chat>

    fun addUser(userId: String, chatId: String): Mono<Unit>

    fun removeUser(userId: String, chatId: String): Mono<Unit>

    fun addMessage(messageId: String, chatId: String): Mono<Unit>

    fun removeMessage(messageId: String, chatId: String): Mono<Unit>

    fun removeMessages(ids: List<String>, chatId: String): Mono<Unit>

    fun delete(id: String): Mono<Unit>

    fun findAll(): Flux<Chat>

    fun findChatsByUserId(userId: String): Flux<Chat>

    fun findMessagesByUserIdAndChatId(userId: String, chatId: String): Flux<Message>

    fun findMessagesFromChat(chatId: String): Flux<Message>

    fun deleteMessagesFromChatByUserId(chatId: String, userId: String): Mono<Unit>
}
