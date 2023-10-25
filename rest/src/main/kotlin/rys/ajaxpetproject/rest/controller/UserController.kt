package rys.ajaxpetproject.rest.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
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
