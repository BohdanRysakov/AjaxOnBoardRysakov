package rys.ajaxpetproject.message.application.port.out

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.message.domain.entity.Message

interface IMessageServiceOutPort {
    fun findMessageById(id: String): Mono<Message>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, message: Message): Mono<Message>

    fun delete(id: String): Mono<Unit>

    fun findMessagesByIds(ids: List<String>): Flux<Message>

    fun deleteMessagesByIds(ids: List<String>): Mono<Unit>
}
