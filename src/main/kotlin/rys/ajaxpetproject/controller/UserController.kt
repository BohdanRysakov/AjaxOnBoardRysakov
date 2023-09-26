package rys.ajaxpetproject.controller


import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.service.UserService

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @GetMapping("/")
    fun getAllUsers(): ResponseEntity<List<MongoUser>> =
        ResponseEntity(userService.findAllUsers(), HttpStatus.OK)

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: ObjectId): ResponseEntity<MongoUser> =
        ResponseEntity(userService.findUserById(id), HttpStatus.OK)

    @PostMapping("/")
    @Validated
    fun createUser(@Valid @RequestBody mongoUser: MongoUser): ResponseEntity<MongoUser> {
        val user = userService.createUser(mongoUser)
        println(user)
        return ResponseEntity(user, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: ObjectId,
        @Valid @RequestBody updatedMongoUser: MongoUser
    ): ResponseEntity<MongoUser> =
        ResponseEntity(userService.updateUser(id, updatedMongoUser), HttpStatus.OK)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: ObjectId): ResponseEntity<Boolean> =
        ResponseEntity(userService.deleteUser(id), HttpStatus.OK)

    @DeleteMapping("/all/")
    fun deleteAllUsers(): ResponseEntity<Boolean> =
        ResponseEntity(userService.deleteAllUsers(), HttpStatus.OK)

}
