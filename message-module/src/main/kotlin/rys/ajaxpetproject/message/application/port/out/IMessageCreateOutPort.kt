package rys.ajaxpetproject.message.application.port.out

import reactor.core.publisher.Mono
import rys.ajaxpetproject.message.domain.entity.Message

interface IMessageCreateOutPort {
    fun create(message: Message): Mono<Message>
}
