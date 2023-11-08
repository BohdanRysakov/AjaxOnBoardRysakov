package rys.ajaxpetproject.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.exceptions.ChatNotFoundException
import rys.ajaxpetproject.kafka.MessageCreateEventProducer
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.UserService
import rys.ajaxpetproject.utils.createEvent

@Service
@Suppress("TooManyFunctions")
class ChatServiceImpl(
    private val chatRepository: ChatRepository,
    private val userService: UserService,
    private val messageService: MessageService,
    private val kafkaEventSender: MessageCreateEventProducer
) : ChatService {
    override fun findChatById(id: String): Mono<MongoChat> {
        return chatRepository.findChatById(id)
    }

    override fun getChatById(id: String): Mono<MongoChat> {
        return chatRepository.findChatById(id)
            .switchIfEmpty {
                Mono.error(ChatNotFoundException("Chat with id $id not found"))
            }
    }

    override fun save(chat: MongoChat): Mono<MongoChat> {
        return chatRepository.save(chat.copy(id = null))
    }

    override fun deleteAll(): Mono<Unit> {
        return chatRepository.deleteAll()
    }

    override fun update(id: String, chat: MongoChat): Mono<MongoChat> {
        return getChatById(id)
            .flatMap { chatRepository.update(id, chat) }
    }

    override fun addUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .then(chatRepository.addUser(userId, chatId))
    }

    override fun removeUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .then(chatRepository.removeUser(userId, chatId))
    }

    override fun addMessage(messageId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            getChatById(chatId),
            messageService.getMessageById(messageId)
        )
            .then(chatRepository.addMessage(messageId, chatId))
            .flatMap<Unit> {
                messageService.getMessageById(messageId)
                    .flatMap {
                        kafkaEventSender.sendCreateEvent(
                            it.createEvent(chatId)
                        )
                    }
            }
            .onErrorResume {
                logger.error("Error while adding message to chat: {}", it.message, it)
                Mono.error(it)
            }
            .thenReturn(Unit)
    }

    override fun removeMessage(messageId: String, chatId: String): Mono<Unit> {
        return chatRepository.removeMessage(messageId, chatId)
    }

    override fun delete(id: String): Mono<Unit> {
        return getChatById(id)
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
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .thenMany(
                getMessagesInChat(chatId).filter { it.userId == userId }
            )
    }

    override fun getMessagesInChat(chatId: String): Flux<MongoMessage> {
        return getChatById(chatId)
            .thenMany(chatRepository.findMessagesFromChat(chatId))
    }

    override fun deleteAllFromUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .then(
                getMessagesInChat(chatId)
                    .filter { it.userId == userId }
                    .mapNotNull { it.id }
                    .map { messageService.delete(it.toString()) }
                    .then(Unit.toMono())
            )
    }

    companion object {
        val logger = LoggerFactory.getLogger(ChatServiceImpl::class.java)
    }
}
