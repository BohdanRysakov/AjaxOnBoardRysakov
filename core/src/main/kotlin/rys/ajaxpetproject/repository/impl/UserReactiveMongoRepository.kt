package rys.ajaxpetproject.repository.impl

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository

@Repository
class UserReactiveMongoRepository(private val mongoTemplate: ReactiveMongoTemplate) : UserRepository {
    override fun findById(id: ObjectId): Mono<MongoUser> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.findById<MongoUser>(query)
    }

    override fun findByName(name: String): Mono<MongoUser> {
        val query = Query.query(Criteria.where("userName").`is`(name))
        return mongoTemplate.findOne<MongoUser>(query)
    }

    override fun save(user: MongoUser): Mono<MongoUser> = mongoTemplate.save(user)

    override fun deleteAll(): Mono<Boolean> {
        return mongoTemplate.remove<MongoUser>(Query())
            .flatMap { deleteResult ->
                if (deleteResult.wasAcknowledged()) {
                    findAll().count().map { count -> count == 0L }
                } else {
                    Mono.just(false)
                }
            }
    }

    override fun update(id: ObjectId, user: MongoUser): Mono<MongoUser> {
        val query = Query.query(Criteria.where("id").`is`(id))
        val updatedUser = user.copy(id = id)
        val findAndModifyOptions = FindAndModifyOptions.options().returnNew(true)
        val updateDef = Update()
            .set("userName", updatedUser.userName)
            .set("password", updatedUser.password)

        return mongoTemplate.findAndModify<MongoUser>(
            query,
            updateDef,
            findAndModifyOptions
        )
    }

    override fun delete(id: ObjectId): Mono<Boolean> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.remove<MongoUser>(query)
            .map { it.wasAcknowledged() && it.deletedCount == 1L }
    }

    override fun findAll(): Flux<MongoUser> = mongoTemplate.findAll<MongoUser>()
}
