package rys.ajaxpetproject.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import rys.ajaxpetproject.exceptions.MessageNotFoundException
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository
import rys.ajaxpetproject.service.MessageService

@Service
class MessageServiceImpl(private val messageRepository: MessageRepository) : MessageService {
    override fun findMessageById(id: String): Mono<MongoMessage> {
        return messageRepository.findMessageById(id)
    }

    override fun getMessageById(id: String): Mono<MongoMessage> {
        return messageRepository.findMessageById(id)
            .switchIfEmpty { Mono.error(MessageNotFoundException("Message with id $id not found")) }
    }

    override fun create(message: MongoMessage): Mono<MongoMessage> {
        return messageRepository.save(message)
    }

    override fun deleteAll(): Mono<Unit> {
        return messageRepository.deleteAll()
    }

    override fun update(id: String, message: MongoMessage): Mono<MongoMessage> {
        return messageRepository.update(id, message)
            .switchIfEmpty {
                Mono.error(MessageNotFoundException("Message with id $id not found"))
            }
    }

    override fun delete(id: String): Mono<Unit> {
        return messageRepository.delete(id)
    }

    override fun findMessagesByIds(ids: List<String>): Flux<MongoMessage> {
        return messageRepository.findMessagesByIds(ids)
    }

    override fun deleteMessagesByIds(ids: List<String>): Mono<Unit> {
        return messageRepository.deleteMessagesByIds(ids)
    }
}
