package rys.ajaxpetproject.controller


import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(@Autowired val userService: UserService) {

    @GetMapping("/")
    fun getAllUsers(): ResponseEntity<List<User>> =
        ResponseEntity(userService.getAllUsers(), HttpStatus.OK)


    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<User> =
        ResponseEntity(userService.getUserById(id), HttpStatus.OK)


    @PostMapping("/")
    @Validated
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<User> =
         ResponseEntity(userService.createUser(user), HttpStatus.CREATED)

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: UUID, @Valid @RequestBody updatedUser: User): ResponseEntity<User> =
        ResponseEntity(userService.updateUser(id, updatedUser), HttpStatus.OK)



    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Boolean> =
        ResponseEntity(userService.deleteUser(id), HttpStatus.OK)

}
