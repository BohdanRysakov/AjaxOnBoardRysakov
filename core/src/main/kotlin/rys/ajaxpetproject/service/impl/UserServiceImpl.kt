package rys.ajaxpetproject.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import rys.ajaxpetproject.exceptions.UserAlreadyExistsException
import rys.ajaxpetproject.exceptions.UserNotFoundException
import rys.ajaxpetproject.internalapi.mongodb.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.UserService

@Service
@Suppress("MagicNumber")
class UserServiceImpl(
    private val encoder: PasswordEncoder,
    private val userRepository: UserRepository
) : UserService {

    override fun createUser(mongoUser: MongoUser): Mono<MongoUser> {
        return userRepository.findByName(mongoUser.userName)
            .handle<MongoUser> { _, sync ->
                sync.error(UserAlreadyExistsException("A user with the username ${mongoUser.userName} already exists!"))
            }
            .switchIfEmpty {
                (mongoUser.password.length > MIN_PASSWORD_LENGTH)
                    .let {
                        val nesUser = mongoUser.copy(password = encoder.encode(mongoUser.password))
                        userRepository.save(nesUser)
                    }
                    .switchIfEmpty {
                        Mono.error(
                            IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters long!")
                        )
                    }
            }
    }

    override fun findUserById(id: String): Mono<MongoUser> {
        return userRepository.findById(id)
    }

    override fun findUserByName(name: String): Mono<MongoUser> {
        return userRepository.findByName(name)
    }

    override fun getUserById(id: String): Mono<MongoUser> {
        return userRepository.findById(id)
            .switchIfEmpty {
                Mono.error(UserNotFoundException("User with id $id does not exist!"))
            }
    }

    override fun getUserByName(name: String): Mono<MongoUser> {
        return userRepository.findByName(name)
            .switchIfEmpty {
                Mono.error(UserNotFoundException("User with name $name does not exist!"))
            }
    }

    override fun findAllUsers(): Flux<MongoUser> {
        return userRepository.findAll()
    }

    override fun updateUser(id: String, updatedUser: MongoUser): Mono<MongoUser> {
        return getUserById(id)
            .flatMap {
                userRepository.update(
                    id,
                    updatedUser.copy(
                        id = it.id,
                        password = encoder.encode(updatedUser.password)
                    )
                )
            }
    }

    override fun deleteUser(id: String): Mono<Unit> {
        return userRepository.delete(id)
    }

    override fun deleteAll(): Mono<Unit> {
        return userRepository.deleteAll()
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }
}
