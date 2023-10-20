package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser

interface UserService {
    fun createUser(mongoUser: MongoUser): Mono<MongoUser>

    fun findUserById(id: ObjectId): Mono<MongoUser>

    fun findUserByName(name: String): Mono<MongoUser>

    fun getUserById(id: ObjectId): Mono<MongoUser>

    fun getUserByName(name: String): Mono<MongoUser>

    fun findAllUsers(): Flux<MongoUser>

    fun updateUser(id: ObjectId, updatedUser: MongoUser): Mono<MongoUser>

    fun deleteUser(id: ObjectId): Mono<Boolean>

    fun deleteAll(): Mono<Boolean>
}
