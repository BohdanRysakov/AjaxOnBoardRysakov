package rys.ajaxpetproject.repository.impl

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.MessageRepository

@Repository
class MessageReactiveRepository(private val mongoTemplate : ReactiveMongoTemplate) : MessageRepository {
    override fun findMessageById(id: ObjectId): Mono<MongoMessage> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.findById<MongoMessage>(query)
    }

    override fun save(message: MongoMessage): Mono<MongoMessage> {
        return mongoTemplate.save(message)
    }

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

    override fun update(id: ObjectId, message: MongoMessage): Mono<MongoMessage> {
        val query = Query.query(Criteria.where("id").`is`(id))
        val updatedMessage = message.copy(id = id)
        val findAndModifyOptions = FindAndModifyOptions.options().returnNew(true)
        val updateDef = Update()
            .set("userId", updatedMessage.userId)
            .set("content", updatedMessage.content)
            .set("sentAt", updatedMessage.sentAt)
        return mongoTemplate.findAndModify<MongoMessage>(
            query,
            updateDef,
            findAndModifyOptions
        )
    }

    override fun delete(id: ObjectId): Mono<Boolean> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.remove<MongoMessage>(query)
            .map { it.wasAcknowledged() && it.deletedCount == 1L }
    }

    override fun findMessagesByIds(ids: List<ObjectId>): Flux<MongoMessage> {
        val query = Query.query(Criteria.where("id").`in`(ids))
        return mongoTemplate.find<MongoMessage>(query)
    }

    private fun findAll(): Flux<MongoMessage> = mongoTemplate.findAll<MongoMessage>()
}
