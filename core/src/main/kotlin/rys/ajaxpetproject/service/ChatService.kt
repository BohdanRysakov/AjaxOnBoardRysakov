package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage

interface ChatService {
    fun findChatById(id: ObjectId): Mono<MongoChat>

    fun save(chat: MongoChat): Mono<MongoChat>

    fun deleteAll(): Mono<Unit>

    fun update(id: ObjectId, chat: MongoChat): Mono<MongoChat>

    fun addUser(userId: ObjectId, chatId: ObjectId): Mono<Unit>

    fun removeUser(userId: ObjectId, chatId: ObjectId): Mono<Unit>

    fun delete(id: ObjectId): Mono<Unit>

    fun findAll(): Flux<MongoChat>

    fun findChatsByUserId(userId: ObjectId): Flux<MongoChat>

    fun findMessagesFromUser(userId: ObjectId, chatId: ObjectId): Flux<MongoMessage>

    fun findMessagesInChat(chatId: ObjectId) : Flux<MongoMessage>

    fun deleteAllFromUser(userId: ObjectId, chatId : ObjectId): Mono<Unit>

}
