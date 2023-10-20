package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage

interface MessageService {
    fun findMessageById(id: ObjectId): Mono<MongoMessage>

    fun getMessageById(id: ObjectId): Mono<MongoMessage>

    fun create(message: MongoMessage): Mono<MongoMessage>

    fun deleteAll(): Mono<Boolean>

    fun deleteAllFromUser(userId: ObjectId, chatId : ObjectId): Mono<Boolean>

    fun update(id: ObjectId, message: MongoMessage): Mono<MongoMessage>

    fun delete(id: ObjectId): Mono<Boolean>

    fun findMessagesFromUser(userId: ObjectId): Flux<MongoMessage>

    fun getMessagesFromUser(userId: ObjectId): Flux<MongoMessage>
}
