package rys.ajaxpetproject.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser

@Suppress("TooManyFunctions")
interface UserRepository {
    fun findById(id: String): Mono<MongoUser>

    fun findByName(name: String): Mono<MongoUser>

    fun save(user: MongoUser): Mono<MongoUser>

    fun deleteAll(): Mono<Unit>

    fun update(id: String, user: MongoUser): Mono<MongoUser>

    fun delete(id: String): Mono<Unit>

    fun findAll(): Flux<MongoUser>
}
