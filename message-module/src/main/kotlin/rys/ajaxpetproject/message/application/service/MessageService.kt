package rys.ajaxpetproject.message.application.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import rys.ajaxpetproject.message.application.port.`in`.IMessageServiceInPort
import rys.ajaxpetproject.message.application.port.out.IMessageServiceOutPort
import rys.ajaxpetproject.message.domain.entity.Message
import rys.ajaxpetproject.exceptions.MessageNotFoundException

@Service
class MessageService(private val messageRepository: IMessageServiceOutPort) : IMessageServiceInPort {
    override fun findMessageById(id: String): Mono<Message> {
        return messageRepository.findMessageById(id)
    }

    override fun getMessageById(id: String): Mono<Message> {
        return findMessageById(id)
            .switchIfEmpty {
                Mono.error(MessageNotFoundException("Message with id $id not found"))
            }
    }

    override fun deleteAll(): Mono<Unit> {
        return messageRepository.deleteAll()
    }

    override fun update(id: String, message: Message): Mono<Message> {
        return messageRepository.update(id, message)
            .switchIfEmpty {
                Mono.error(MessageNotFoundException("Message with id $id not found"))
            }
    }

    override fun delete(id: String): Mono<Unit> {
        return messageRepository.delete(id)
    }

    override fun findMessagesByIds(ids: List<String>): Flux<Message> {
        return messageRepository.findMessagesByIds(ids)
    }

    override fun deleteMessagesByIds(ids: List<String>): Mono<Unit> {
        return messageRepository.deleteMessagesByIds(ids)
    }
}
