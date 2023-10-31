package rys.ajaxpetproject.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.exceptions.ChatNotFoundException
import rys.ajaxpetproject.exceptions.UserNotFoundException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.UserService

@Service
@Suppress("TooManyFunctions")
class ChatServiceImpl(
    private val chatRepository: ChatRepository,
    private val userService: UserService,
    private val messageService: MessageService
) : ChatService {
    override fun findChatById(id: String): Mono<MongoChat> {
        return chatRepository.findChatById(id)
    }

    override fun save(chat: MongoChat): Mono<MongoChat> {
        return chatRepository.save(chat)
    }

    override fun deleteAll(): Mono<Unit> {
        return chatRepository.deleteAll()
    }

    override fun update(id: String, chat: MongoChat): Mono<MongoChat> {
        return findChatById(id)
            .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $id not found")) }
            .flatMap { chatRepository.update(id, chat) }
    }

    override fun addUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty { Mono.error(UserNotFoundException("User with id $userId not found")) },
            findChatById(chatId)
                .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $chatId not found")) }
        )
            .then(chatRepository.addUser(userId, chatId))
    }

    override fun removeUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty { Mono.error(UserNotFoundException("User with id $userId not found")) },
            findChatById(chatId)
                .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $chatId not found")) }
        )
            .then(chatRepository.removeUser(userId, chatId))
    }

    override fun addMessage(messageId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            findChatById(chatId)
                .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $chatId not found")) },
            messageService.findMessageById(messageId)
                .switchIfEmpty { Mono.error(ChatNotFoundException("Message with id $messageId not found")) }
        )
            .then(chatRepository.addMessage(messageId, chatId))
    }

    override fun removeMessage(messageId: String, chatId: String): Mono<Unit> {
        return chatRepository.removeMessage(messageId, chatId)
    }

    override fun delete(id: String): Mono<Unit> {
        return findChatById(id)
            .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $id not found")) }
            .then(chatRepository.delete(id))
    }

    override fun findAll(): Flux<MongoChat> {
        return chatRepository.findAll()
    }

    override fun findChatsByUserId(userId: String): Flux<MongoChat> {
        return chatRepository.findChatsByUserId(userId)
    }

    override fun getMessagesFromChatByUser(userId: String, chatId: String): Flux<MongoMessage> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty { Mono.error(UserNotFoundException("User with id $userId not found")) },
            findChatById(chatId)
                .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $chatId not found")) }
        )
            .thenMany(
                getMessagesInChat(chatId).filter { it.userId == userId }
            )
    }

    override fun getMessagesInChat(chatId: String): Flux<MongoMessage> {
        return findChatById(chatId)
            .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $chatId not found")) }
            .thenMany(chatRepository.findMessagesFromChat(chatId))
    }

    override fun deleteAllFromUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.getUserById(userId),
            findChatById(chatId)
                .switchIfEmpty { Mono.error(ChatNotFoundException("Chat with id $chatId not found")) }
        )
            .then(
                getMessagesInChat(chatId)
                    .filter { it.userId == userId }
                    .mapNotNull { it.id }
                    .map { messageService.delete(it.toString()) }
                    .then(Unit.toMono())
            )
    }
}
