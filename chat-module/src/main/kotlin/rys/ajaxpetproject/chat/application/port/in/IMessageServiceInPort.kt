package rys.ajaxpetproject.chat.application.port.`in`

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.domain.entity.Message

interface IMessageServiceInPort {
    fun getMessageById(id: String): Mono<Message>

    fun getMessagesByIds(ids: List<String>): Flux<Message>

    fun delete(id : String) : Mono<Unit>
}
