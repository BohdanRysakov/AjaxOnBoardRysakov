package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser

@Repository
@Suppress("TooManyFunctions")
class UserRepoImplement : UserRepository {
    override fun findById(id: ObjectId): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun save(user: MongoUser): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun deleteAll(): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun update(id: ObjectId, user: MongoUser): Mono<MongoUser> {
        TODO("Not yet implemented")
    }

    override fun delete(id: ObjectId): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Flux<MongoUser> {
        TODO("Not yet implemented")
    }
}
