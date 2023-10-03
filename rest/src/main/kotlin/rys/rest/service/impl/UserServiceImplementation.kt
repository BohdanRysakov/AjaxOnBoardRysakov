package rys.rest.service.impl

import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import rys.rest.exceptions.UserNotFoundException
import rys.rest.model.MongoUser
import rys.rest.repository.UserRepository
import rys.rest.service.UserService

@Service
class UserServiceImplementation(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder) : UserService {

    override fun createUser(mongoUser: MongoUser): MongoUser {
        return userRepository.save(mongoUser.copy(password = passwordEncoder.encode(mongoUser.password)))
    }

    override fun getUserById(id: ObjectId): MongoUser = userRepository.findUserById(id) ?: throw UserNotFoundException()

    override fun findUserById(id: ObjectId): MongoUser? = userRepository.findUserById(id)

    override fun findAllUsers(): List<MongoUser> = userRepository.findAllBy()

    override fun updateUser(id: ObjectId, updatedMongoUser: MongoUser): MongoUser? =
        findUserById(id)?.let {
            userRepository.save(updatedMongoUser.copy(id = id))
        } ?: throw UserNotFoundException()

    override fun deleteUser(id: ObjectId): Boolean {
        return userRepository.deleteUserById(id)
    }

    override fun deleteUsers(): Boolean {
        userRepository.deleteAll()
        return true
    }
}
