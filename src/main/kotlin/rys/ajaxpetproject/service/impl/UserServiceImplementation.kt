package rys.ajaxpetproject.service.impl

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.UserNotFoundException
import rys.ajaxpetproject.exception.UsersNotFoundException
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.UserService
import java.util.*

@Service
class UserServiceImplementation(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder) : UserService {


    override fun createUser(user: User): User {
        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user);
    }

    override fun getUserById(id: UUID): User = userRepository.findUserById(id) ?: throw UserNotFoundException()
    override fun findUserById(id: UUID): User? = userRepository.findUserById(id)

    override fun getAllUsers(): List<User> = userRepository.findAllBy() ?: throw UsersNotFoundException()


    override fun findAllUsers(): List<User>? = userRepository.findAllBy()

    override fun updateUser(id: UUID, updatedUser: User): User? =
        getUserById(id).let { userRepository.save(updatedUser) }


    override fun deleteUser(id: UUID): Boolean {
        return userRepository.deleteUserById(id)
    }
     override fun deleteUsers(): Unit {
        return userRepository.deleteAll()
    }
}
