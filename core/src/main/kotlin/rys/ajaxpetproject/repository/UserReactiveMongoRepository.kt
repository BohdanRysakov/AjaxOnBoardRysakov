package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoUser

@Repository
class UserReactiveMongoRepository(private val mongoTemplate: ReactiveMongoTemplate) : UserRepository {
    override fun findById(id: ObjectId): Mono<MongoUser> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.findOne(query, MongoUser::class.java)
    }

    override fun findByName(name: String): Mono<MongoUser> {
        val query = Query.query(Criteria.where("userName").`is`(name))
        return mongoTemplate.findOne(query, MongoUser::class.java)
    }

    override fun save(user: MongoUser): Mono<MongoUser> = mongoTemplate.save(user)

    override fun deleteAll() : Mono<Boolean> {
        val query = Query()
        return mongoTemplate.remove(query, MongoUser::class.java)
            .map { it.wasAcknowledged() && it.deletedCount == 1L }
    }

    override fun update(id: ObjectId, user: MongoUser): Mono<MongoUser> {
        val query = Query.query(Criteria.where("id").`is`(id))
        val updatedUser = user.copy(id = id)
        val findAndModifyOptions = FindAndModifyOptions.options().returnNew(true)
        val updateDef = Update()
            .set("userName", updatedUser.userName)
            .set("password", updatedUser.password)

        return mongoTemplate.findAndModify(
            query,
            updateDef,
            findAndModifyOptions,
            MongoUser::class.java
        )
    }

    override fun delete(id: ObjectId): Mono<Boolean> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.remove(query, MongoUser::class.java)
            .map { it.wasAcknowledged() && it.deletedCount == 1L }
    }

    override fun findAll(): Flux<MongoUser> = mongoTemplate.findAll(MongoUser::class.java)
}
