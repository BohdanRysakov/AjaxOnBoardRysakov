package rys.ajaxpetproject.chat.application.service

import com.google.protobuf.Timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.`in`.IChatServiceInPort
import rys.ajaxpetproject.chat.application.port.`in`.IMessageServiceInPort
import rys.ajaxpetproject.chat.application.port.out.IChatEventPublisherOutPort
import rys.ajaxpetproject.chat.application.port.out.IChatServiceOutPort
import rys.ajaxpetproject.chat.domain.entity.Chat
import rys.ajaxpetproject.chat.domain.entity.Message
import rys.ajaxpetproject.exceptions.ChatNotFoundException
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreatedEvent
import rys.ajaxpetproject.service.UserService
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage

@Service
@Suppress("TooManyFunctions")
class ChatService(
    private val chatRepository: IChatServiceOutPort,
    private val userService: UserService,
    private val messageService: IMessageServiceInPort,
    private val kafkaEventSender: IChatEventPublisherOutPort
) : IChatServiceInPort {

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
            .flatMap<Unit> {
                messageService.getMessageById(messageId)
                    .flatMap {
                        kafkaEventSender.sendEvent(
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
            .thenMany(chatRepository.getMessagesInChat(chatId))
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

    private fun Message.createEvent(chatId: String): MessageCreatedEvent {
        return MessageCreatedEvent.newBuilder()
            .setChatId(chatId)
            .setMessage(this.toProto())
            .build()
    }

    private fun Message.toProto(): ProtoMessage {
        val message = this@toProto
        return ProtoMessage.newBuilder().apply {
            this.userId = message.userId
            this.content = message.content
            this.sentTime = Timestamp.newBuilder().setSeconds(message.sentAt.time).build()
        }.build()
    }


    companion object {
        val logger: Logger = LoggerFactory.getLogger(ChatService::class.java)
    }
}

