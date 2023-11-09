package rys.ajaxpetproject.chat.application.port.`in`

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.domain.entity.Message

interface IMessageServiceInPort {
    fun findMessageById(id: String): Mono<Message>

    fun getMessageById(id: String): Mono<Message>

    fun create(message: Message): Mono<Message>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, message: Message): Mono<Message>

    fun delete(id: String): Mono<Unit>

    fun findMessagesByIds(ids: List<String>): Flux<Message>

    fun deleteMessagesByIds(ids: List<String>): Mono<Unit>
}
