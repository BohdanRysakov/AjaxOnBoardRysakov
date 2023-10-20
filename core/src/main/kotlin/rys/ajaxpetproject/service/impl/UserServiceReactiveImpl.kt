package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.exceptions.UserAlreadyExistsException
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
        return userRepository.findByName(mongoUser.userName!!)
            .flatMap {
                Mono.error<MongoUser>(
                    UserAlreadyExistsException("A user with the username ${mongoUser.userName} already exists!")
                )
            }
            .switchIfEmpty(
                (mongoUser.password!!.length > 8)
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
        TODO("Not yet implemented")
    }

    override fun findUserByName(name: String): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun getUserById(id: ObjectId): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun getUserByName(name: String): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun findAllUsers(): Flux<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun updateUser(id: ObjectId, updatedUser: MongoUser): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun deleteUser(id: ObjectId): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteUsers(): Mono<Boolean> {
        TODO("Not yet implemented")
    }
}
