package rys.ajaxpetproject.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage

interface MessageService {
    fun findMessageById(id: String): Mono<MongoMessage>

    fun getMessageById(id: String): Mono<MongoMessage>

    fun create(message: MongoMessage): Mono<MongoMessage>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, message: MongoMessage): Mono<MongoMessage>

    fun delete(id: String): Mono<Unit>

    fun findMessagesByIds(ids: List<String>): Flux<MongoMessage>

    fun deleteMessagesByIds(ids: List<String>): Mono<Unit>
}
