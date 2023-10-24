package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
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
    override fun findChatById(id: ObjectId): Mono<MongoChat> {
        return chatRepository.findChatById(id)
    }

    override fun save(chat: MongoChat): Mono<MongoChat> {
        return chatRepository.save(chat)
    }

    override fun deleteAll(): Mono<Unit> {
        return chatRepository.deleteAll()
    }

    override fun update(id: ObjectId, chat: MongoChat): Mono<MongoChat> {
        return findChatById(id)
            .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $id not found")))
            .flatMap { chatRepository.update(id, chat) }
    }

    override fun addUser(userId: ObjectId, chatId: ObjectId): Mono<Unit> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty(Mono.error(UserNotFoundException("User with id $userId not found"))),
            findChatById(chatId)
                .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $chatId not found")))
        )
            .then(chatRepository.addUser(userId, chatId))
    }

    override fun removeUser(userId: ObjectId, chatId: ObjectId): Mono<Unit> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty(Mono.error(UserNotFoundException("User with id $userId not found"))),
            findChatById(chatId)
                .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $chatId not found")))
        )
            .then(chatRepository.removeUser(userId, chatId))
    }

    override fun delete(id: ObjectId): Mono<Unit> {
        return Mono.`when`(
            findChatById(id)
                .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $id not found")))
        )
            .then(chatRepository.delete(id))
    }

    override fun findAll(): Flux<MongoChat> {
        return chatRepository.findAll()
    }

    override fun findChatsByUserId(userId: ObjectId): Flux<MongoChat> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty(Mono.error(UserNotFoundException("User with id $userId not found")))
        )
            .thenMany(chatRepository.findChatsByUserId(userId))
    }

    override fun findMessagesFromUser(userId: ObjectId, chatId: ObjectId): Flux<MongoMessage> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty(Mono.error(UserNotFoundException("User with id $userId not found"))),
            findChatById(chatId)
                .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $chatId not found")))
        )
            .thenMany(
                findMessagesInChat(chatId).filter{it.userId== userId}
            )
    }

    override fun findMessagesInChat(chatId: ObjectId): Flux<MongoMessage> {
        return Mono.`when`(
            findChatById(chatId)
                .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $chatId not found")))
        )
            .thenMany(chatRepository.findMessagesFromChat(chatId))
    }

    override fun deleteAllFromUser(userId: ObjectId, chatId: ObjectId): Mono<Unit> {
        return Mono.`when`(
            userService.findUserById(userId)
                .switchIfEmpty(Mono.error(UserNotFoundException("User with id $userId not found"))),
            findChatById(chatId)
                .switchIfEmpty(Mono.error(ChatNotFoundException("Chat with id $chatId not found")))
        )
            .then(
                findMessagesInChat(chatId)
                    .filter { it.userId == userId }
                    .mapNotNull { it.id }
                    .flatMap { messageService.delete(it!!)}
                    .then(Unit.toMono())
            )
    }
}
