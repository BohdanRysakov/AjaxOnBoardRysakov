package rys.ajaxpetproject.chat.application.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import rys.ajaxpetproject.chat.application.mapper.createEvent
import rys.ajaxpetproject.chat.application.mapper.toProto
import rys.ajaxpetproject.chat.application.port.`in`.ChatServiceInPort
import rys.ajaxpetproject.chat.application.port.out.EventSenderOutPort
import rys.ajaxpetproject.chat.application.port.out.ChatServiceOutPort
import rys.ajaxpetproject.chat.application.port.out.MessageServiceOutPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.internalapi.exceptions.ChatNotFoundException
import rys.ajaxpetproject.service.UserService

@Service
@Suppress("TooManyFunctions")
class ChatService(
    private val chatRepository: ChatServiceOutPort,
    private val messageService: MessageServiceOutPort,
    private val userService: UserService,
    private val eventSender: EventSenderOutPort
) : ChatServiceInPort {
    override fun findChatById(id: String): Mono<Chat> {
        return chatRepository.findChatById(id)
    }

    override fun getChatById(id: String): Mono<Chat> {
        return chatRepository.findChatById(id)
            .switchIfEmpty {
                Mono.error(ChatNotFoundException("Chat with id $id not found"))
            }
    }

    override fun save(chat: Chat): Mono<Chat> {
        return chatRepository.save(chat.copy(id = null))
    }

    override fun deleteAll(): Mono<Unit> {
        return chatRepository.deleteAll()
    }

    override fun update(id: String, chat: Chat): Mono<Chat> {
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
            .flatMap {
                messageService.getMessageById(messageId)
                    .flatMap {
                        eventSender.sendCreateEvent(
                            it.createEvent(chatId).toProto()
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

    override fun findAll(): Flux<Chat> {
        return chatRepository.findAll()
    }

    override fun findChatsByUserId(userId: String): Flux<Chat> {
        return chatRepository.findChatsByUserId(userId)
    }

    override fun getMessagesFromChatByUser(userId: String, chatId: String): Flux<Message> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .thenMany(
                getMessagesInChat(chatId).filter { it.userId == userId }
            )
    }

    override fun getMessagesInChat(chatId: String): Flux<Message> {
        return getChatById(chatId)
            .thenMany(chatRepository.findMessagesFromChat(chatId))
    }

    override fun deleteAllFromUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        ).then(chatRepository.deleteMessagesFromChatByUserId(chatId, userId))
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ChatService::class.java)
    }
}
