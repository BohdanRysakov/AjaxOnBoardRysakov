package rys.ajaxpetproject.rest.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
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
    @PostMapping("/")
    fun createUser(@Valid @RequestBody mongoUser: MongoUser): Mono<ResponseEntity<MongoUser>> =
        userService.createUser(mongoUser)
            .map { user -> ResponseEntity(user, HttpStatus.CREATED) }

    @GetMapping("/{id}")
    fun findUserById(@PathVariable id: ObjectId): Mono<ResponseEntity<MongoUser>> =
        userService.findUserById(id)
            .map { user -> ResponseEntity(user, HttpStatus.OK) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @GetMapping("/name/{name}")
    fun findUserByName(@PathVariable name: String): Mono<ResponseEntity<MongoUser>> =
        userService.findUserByName(name)
            .map { user -> ResponseEntity(user, HttpStatus.OK) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @GetMapping("/")
    fun findAllUsers(): Flux<MongoUser> = userService.findAllUsers()

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: ObjectId,
                   @Valid @RequestBody updatedUser: MongoUser): Mono<ResponseEntity<MongoUser>> =
        userService.updateUser(id, updatedUser)
            .map { user -> ResponseEntity(user, HttpStatus.OK) }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: ObjectId): Mono<ResponseEntity<Void>> =
        userService.deleteUser(id)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))
}
