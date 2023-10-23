package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage

interface ChatRepository {
    fun findChatById(id: ObjectId): Mono<MongoChat>

    fun save(chat: MongoChat): Mono<MongoChat>

    fun deleteAll(): Mono<Unit>

    fun update(id: ObjectId, chat: MongoChat): Mono<MongoChat>

    fun delete(id: ObjectId): Mono<Unit>

    fun findAll(): Flux<MongoChat>

    fun findChatsByUserId(userId: ObjectId): Flux<MongoChat>

    fun findMessagesByUserIdAndChatId(userId: ObjectId, chatId: ObjectId): Flux<MongoMessage>
}
