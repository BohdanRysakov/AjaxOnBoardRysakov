package rys.ajaxpetproject.application.services

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.domain.entity.User

interface UserService {
    fun createUser(user: User): Mono<User>

    fun findUserById(id: String): Mono<User>

    fun findUserByName(name: String): Mono<User>

    fun getUserById(id: String): Mono<User>

    fun getUserByName(name: String): Mono<User>

    fun findAllUsers(): Flux<User>

    fun updateUser(id: String, updatedUser: User): Mono<User>

    fun deleteUser(id: String): Mono<Unit>

    fun deleteAll(): Mono<Unit>
}
