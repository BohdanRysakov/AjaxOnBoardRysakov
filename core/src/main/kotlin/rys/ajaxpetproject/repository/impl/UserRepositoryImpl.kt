package rys.ajaxpetproject.repository.impl

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
class UserRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) :
    UserRepository {
    override fun findById(id: String): Mono<MongoUser> {
        return mongoTemplate.findById<MongoUser>(id)
    }

    override fun findByName(name: String): Mono<MongoUser> {
        val query = Query.query(Criteria.where("userName").`is`(name))
        return mongoTemplate.findOne<MongoUser>(query)
    }

    override fun save(user: MongoUser): Mono<MongoUser> = mongoTemplate.save(user)

    override fun deleteAll(): Mono<Unit> {
        return mongoTemplate.remove<MongoUser>(Query())
            .thenReturn(Unit)
    }

    override fun update(id: String, user: MongoUser): Mono<MongoUser> {
        val query = Query.query(Criteria.where("_id").`is`(id))
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

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove<MongoUser>(query)
            .thenReturn(Unit)
    }

    override fun findAll(): Flux<MongoUser> = mongoTemplate.findAll<MongoUser>()
}
