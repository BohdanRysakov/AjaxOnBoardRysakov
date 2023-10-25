package rys.ajaxpetproject.rest.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.service.UserService

@RestController
@RequestMapping("/users")
class UserController(val userService: UserService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    fun createUser(@Valid @RequestBody mongoUser: MongoUser): Mono<MongoUser> =
        userService.createUser(mongoUser)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/{id}")
    fun findUserById(@PathVariable id: String): Mono<MongoUser> =
        userService.findUserById(id)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/name/{name}")
    fun findUserByName(@PathVariable name: String): Mono<MongoUser> =
        userService.findUserByName(name)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/")
    fun findAllUsers(): Flux<MongoUser> = userService.findAllUsers()

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: String,
                   @Valid @RequestBody updatedUser: MongoUser
    ): Mono<MongoUser> =
        userService.updateUser(id, updatedUser)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): Mono<Unit> =
        userService.deleteUser(id)
}
