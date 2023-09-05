package rys.ajaxpetproject.service

import rys.ajaxpetproject.exception.UserNotFoundException
import rys.ajaxpetproject.exception.UsersNotFoundException
import rys.ajaxpetproject.model.User
import java.util.*
import kotlin.jvm.Throws

interface UserService {

    fun createUser(user: User): User
    @Throws(UserNotFoundException::class)
    fun getUserById(id: UUID): User
    fun findUserById(id: UUID): User?
    @Throws(UsersNotFoundException::class)
    fun getAllUsers(): List<User>
    fun findAllUsers(): List<User>?
    fun updateUser(id: UUID, updatedUser: User): User?
    fun deleteUser(id: UUID): Boolean
    fun deleteUsers()
}
