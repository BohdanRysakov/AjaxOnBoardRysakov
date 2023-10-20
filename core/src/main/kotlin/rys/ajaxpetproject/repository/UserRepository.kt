package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser

@Suppress("TooManyFunctions")
interface UserRepository {
    fun findById(id: ObjectId): Mono<MongoUser>

    fun findByName(name: String): Mono<MongoUser>

    fun save(user: MongoUser): Mono<MongoUser>

    fun deleteAll(): Mono<Boolean>

    fun update(id: ObjectId, user: MongoUser): Mono<MongoUser>

    fun delete(id: ObjectId): Mono<Boolean>

    fun findAll(): Flux<MongoUser>
}
