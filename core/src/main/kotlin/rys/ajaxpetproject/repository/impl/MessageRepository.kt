package rys.ajaxpetproject.repository.impl

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository

@Repository
class MessageRepository(private val mongoTemplate: ReactiveMongoTemplate) :
    MessageRepository {
    override fun findMessageById(id: String): Mono<MongoMessage> {
        return mongoTemplate.findById<MongoMessage>(id)
    }

    override fun save(message: MongoMessage): Mono<MongoMessage> {
        return mongoTemplate.save(message)
    }

    override fun deleteAll(): Mono<Unit> {
        return mongoTemplate.remove<MongoMessage>(Query())
            .thenReturn(Unit)
    }

    override fun update(id: String, message: MongoMessage): Mono<MongoMessage> {
        val query = Query.query(Criteria.where("_id").`is`(id))
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

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove<MongoMessage>(query)
            .thenReturn(Unit)
    }

    override fun findMessagesByIds(ids: List<String>): Flux<MongoMessage> {
        val query = Query.query(Criteria.where("_id").`in`(ids))
        return mongoTemplate.find<MongoMessage>(query)
    }

    override fun deleteMessagesByIds(ids: List<String>): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`in`(ids))
        return mongoTemplate.remove<MongoMessage>(query)
            .thenReturn(Unit)
    }
}
