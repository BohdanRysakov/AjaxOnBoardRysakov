package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.exceptions.UserAlreadyExistsException
import rys.ajaxpetproject.exceptions.UserNotFoundException
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.UserService

@Service
@Suppress("MagicNumber")
class UserServiceReactiveImpl(
    private val encoder: PasswordEncoder,
    private val userRepository: UserRepository
) : UserService {
    override fun createUser(mongoUser: MongoUser): Mono<MongoUser> {
        return userRepository.findByName(mongoUser.userName)
            .flatMap {
                Mono.error<MongoUser>(
                    UserAlreadyExistsException("A user with the username ${mongoUser.userName} already exists!")
                )
            }
            .switchIfEmpty(
                (mongoUser.password.length > 8)
                    .let {
                        val mongoUser2 = mongoUser.copy(password = encoder.encode(mongoUser.password))
                        userRepository.save(
                            mongoUser2
                        )
                    }
                    .switchIfEmpty(
                        Mono.error(
                            IllegalArgumentException("Password must be at least 8 characters long!")
                        )
                    )
            )
    }

    override fun findUserById(id: ObjectId): Mono<MongoUser> {
        return userRepository.findById(id)
    }

    override fun findUserByName(name: String): Mono<MongoUser> {
        return userRepository.findByName(name)
    }

    override fun getUserById(id: ObjectId): Mono<MongoUser> {
        return userRepository.findById(id).switchIfEmpty(
            Mono.error(
                UserNotFoundException("User with id $id does not exist!")
            )
        )
    }

    override fun getUserByName(name: String): Mono<MongoUser> {
        return userRepository.findByName(name).switchIfEmpty(
            Mono.error(
                UserNotFoundException("User with name $name does not exist!")
            )
        )
    }

    override fun findAllUsers(): Flux<MongoUser> {
        return userRepository.findAll()
    }

    override fun updateUser(id: ObjectId, updatedUser: MongoUser): Mono<MongoUser> {
        return getUserById(id)
            .onErrorComplete()
            .flatMap {
                userRepository.update(
                    id,
                    updatedUser.copy(
                        id = it.id,
                        userName = it.userName,
                        password = encoder.encode(it.password)
                    )
                )
            }
    }

    override fun deleteUser(id: ObjectId): Mono<Boolean> {
        return getUserById(id)
            .onErrorComplete()
            .flatMap {
                userRepository.delete(id)
            }
    }

    override fun deleteAll(): Mono<Boolean> {
        return userRepository.deleteAll()
    }
}
