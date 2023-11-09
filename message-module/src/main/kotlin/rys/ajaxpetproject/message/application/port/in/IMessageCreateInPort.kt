package rys.ajaxpetproject.message.application.port.`in`

import reactor.core.publisher.Mono
import rys.ajaxpetproject.message.domain.entity.Message

interface IMessageCreateInPort {
    fun create(message: Message): Mono<Message>
}
