package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage

interface MessageService {
    fun findMessageById(id: ObjectId): Mono<MongoMessage>

    fun getMessageById(id: ObjectId): Mono<MongoMessage>

    fun create(message: MongoMessage): Mono<MongoMessage>

    fun deleteAll(): Mono<Unit>

    fun update(id: ObjectId, message: MongoMessage): Mono<MongoMessage>

    fun delete(id: ObjectId): Mono<Unit>

    fun findMessagesByIds(ids: List<ObjectId>): Flux<MongoMessage>

    fun deleteMessagesByIds(ids: List<ObjectId>): Mono<Unit>
}
