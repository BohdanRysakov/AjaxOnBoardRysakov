package rys.ajaxpetproject.chat.application.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.application.port.`in`.IMessageServiceInPort
import rys.ajaxpetproject.chat.application.port.out.IMessageServiceOutPort
import rys.ajaxpetproject.chat.domain.entity.Message


@Service
class MessageService(private val messageRepository: IMessageServiceOutPort) : IMessageServiceInPort {
    override fun getMessageById(id: String): Mono<Message> {
        return messageRepository.getMessageById(id)
    }

    override fun getMessagesByIds(ids: List<String>): Flux<Message> {
        return messageRepository.getMessagesByIds(ids)
    }

    override fun delete(id: String): Mono<Unit> {
        return messageRepository.delete(id)
    }

}
