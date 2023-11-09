package rys.ajaxpetproject.chat.application.port.`in`

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.domain.entity.Chat
import rys.ajaxpetproject.chat.domain.entity.Message

interface IChatServiceInPort {
    fun findChatById(id: String): Mono<Chat>

    fun getChatById(id: String): Mono<Chat>

    fun save(chat: Chat): Mono<Chat>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, chat: Chat): Mono<Chat>

    fun addUser(userId: String, chatId: String): Mono<Unit>

    fun removeUser(userId: String, chatId: String): Mono<Unit>

    fun addMessage(messageId: String, chatId: String): Mono<Unit>

    fun removeMessage(messageId: String, chatId: String): Mono<Unit>

    fun delete(id: String): Mono<Unit>

    fun findAll(): Flux<Chat>

    fun findChatsByUserId(userId: String): Flux<Chat>

    fun getMessagesFromChatByUser(userId: String, chatId: String): Flux<Message>

    fun getMessagesInChat(chatId: String): Flux<Message>

    fun deleteAllFromUser(userId: String, chatId: String): Mono<Unit>
}
