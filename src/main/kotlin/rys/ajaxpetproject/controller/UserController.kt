package rys.ajaxpetproject.controller


import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import rys.ajaxpetproject.exception.BadIdTypeException
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.service.UserService
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@RestController
@RequestMapping("/users")
class UserController(@Autowired val userService: UserService) {

    @GetMapping("/")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity(users, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<User> {
        try {
            val uuid = UUID.fromString(id)
            val user = userService.getUserById(uuid)
            return ResponseEntity(user, HttpStatus.OK)
        }catch (e : IllegalArgumentException) {
            throw BadIdTypeException()
        }


    }

    @PostMapping("/")
    @Validated
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<User> {
        val newUser = userService.createUser(user)
        return ResponseEntity(newUser, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: UUID, @RequestBody @Valid updatedUser: User): ResponseEntity<User> {
        val existingUser = userService.getUserById(id)

        val user = userService.updateUser(id, updatedUser)
        return ResponseEntity(user, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Boolean> {
        val isDeleted = userService.deleteUser(id)
        return if (isDeleted) {
            ResponseEntity(true, HttpStatus.OK)
        } else {
            ResponseEntity(false, HttpStatus.NOT_FOUND)
        }
    }
}
