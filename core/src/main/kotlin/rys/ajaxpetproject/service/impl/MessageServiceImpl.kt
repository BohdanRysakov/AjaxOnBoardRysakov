package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.exceptions.MessageNotFoundException
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService

class MessageServiceImpl(private val messageRepository: MessageRepository,
    private val chatService: ChatService) : MessageService {
    override fun findMessageById(id: ObjectId): Mono<MongoMessage> {
        return messageRepository.findMessageById(id)
    }

    override fun getMessageById(id: ObjectId): Mono<MongoMessage> {
        return messageRepository.findMessageById(id)
            .switchIfEmpty(MessageNotFoundException("Message with id $id not found").toMono())
    }

    override fun create(message: MongoMessage): Mono<MongoMessage> {
        return messageRepository.save(message)
    }

    override fun deleteAll(): Mono<Boolean> {
        return messageRepository.deleteAll()
    }

    override fun deleteAllFromUser(userId: ObjectId, chatId: ObjectId): Mono<Boolean> {

        val messages = chatService.findMessagesByUserIdAndChatId(userId, chatId).mapNotNull { it.id }

        return messageRepository.(messages)



        return messageRepository.deleteAllFromUser(userId, chatId)
    }

    override fun update(id: ObjectId, message: MongoMessage): Mono<MongoMessage> {
        TODO("Not yet implemented")
    }

    override fun delete(id: ObjectId): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun findMessagesFromUser(userId: ObjectId): Flux<MongoMessage> {
        TODO("Not yet implemented")
    }

    override fun getMessagesFromUser(userId: ObjectId): Flux<MongoMessage> {
        TODO("Not yet implemented")
    }

}
