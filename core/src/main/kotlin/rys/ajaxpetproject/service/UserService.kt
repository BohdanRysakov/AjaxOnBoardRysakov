package rys.ajaxpetproject.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser

interface UserService {
    fun createUser(mongoUser: MongoUser): Mono<MongoUser>

    fun findUserById(id: String): Mono<MongoUser>

    fun findUserByName(name: String): Mono<MongoUser>

    fun getUserById(id: String): Mono<MongoUser>

    fun getUserByName(name: String): Mono<MongoUser>

    fun findAllUsers(): Flux<MongoUser>

    fun updateUser(id: String, updatedUser: MongoUser): Mono<MongoUser>

    fun deleteUser(id: String): Mono<Unit>

    fun deleteAll(): Mono<Unit>
}
