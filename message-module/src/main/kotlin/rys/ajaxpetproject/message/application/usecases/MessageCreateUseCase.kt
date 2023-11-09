package rys.ajaxpetproject.message.application.usecases

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import rys.ajaxpetproject.message.application.port.`in`.IMessageCreateInPort
import rys.ajaxpetproject.message.application.port.out.IMessageCreateOutPort
import rys.ajaxpetproject.message.domain.entity.Message

@Service
class MessageCreateUseCase(
    private val messageRepository: IMessageCreateOutPort
) : IMessageCreateInPort {
    override fun create(message: Message): Mono<Message> {
        return messageRepository.create(message)
    }
}
