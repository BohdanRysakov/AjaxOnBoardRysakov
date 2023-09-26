package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.UserNotFoundException
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserDao
import rys.ajaxpetproject.service.UserService

@Service
class UserServiceImpl(
    private val userRepository: UserDao, private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun createUser(mongoUser: MongoUser): MongoUser {
        return userRepository.save(mongoUser.copy(password = passwordEncoder.encode(mongoUser.password)))
    }

    override fun getUserById(id: ObjectId): MongoUser = userRepository.findUserById(id) ?: throw UserNotFoundException()

    override fun findUserById(id: ObjectId): MongoUser? = userRepository.findUserById(id)

    override fun findAllUsers(): List<MongoUser> = userRepository.findAllUsers()

    override fun updateUser(id: ObjectId, updatedMongoUser: MongoUser): MongoUser? =
        findUserById(id)?.let {
            userRepository.save(updatedMongoUser.copy(id = id))
        } ?: throw UserNotFoundException()

    override fun deleteUser(id: ObjectId): Boolean {
        return userRepository.deleteUserById(id)
    }

    override fun deleteAllUsers(): Boolean {
        userRepository.deleteAllUsers()
        return true
    }
}
