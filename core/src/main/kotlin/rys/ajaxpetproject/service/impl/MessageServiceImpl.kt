package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.exceptions.MessageNotFoundException
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository
import rys.ajaxpetproject.service.MessageService

class MessageServiceImpl(private val messageRepository: MessageRepository) : MessageService {
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

    override fun deleteAll(): Mono<Unit> {
        return messageRepository.deleteAll()
    }

    override fun update(id: ObjectId, message: MongoMessage): Mono<MongoMessage> {
        return findMessageById(id).switchIfEmpty {
            MessageNotFoundException("Message with id $id not found").toMono() }
            .flatMap { messageRepository.update(id, message) }
    }

    override fun delete(id: ObjectId): Mono<Unit> {
        return findMessageById(id).switchIfEmpty {
            MessageNotFoundException("Message with id $id not found").toMono() }
            .flatMap { messageRepository.delete(id) }
    }

    override fun findMessagesByIds(ids: List<ObjectId>): Flux<MongoMessage> {
        return messageRepository.findMessagesByIds(ids)
    }

    override fun deleteMessagesByIds(ids: List<ObjectId>): Mono<Unit>{
        return messageRepository.deleteMessagesByIds(ids)
    }
}
