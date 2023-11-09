package rys.ajaxpetproject.message.infractructure.mongodb.repository

import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.message.application.port.out.IMessageCreateOutPort
import rys.ajaxpetproject.message.application.port.out.IMessageServiceOutPort
import rys.ajaxpetproject.message.domain.entity.Message
import rys.ajaxpetproject.internalapi.mongodb.model.MongoMessage

@Repository
class MessageRepository(private val mongoTemplate: ReactiveMongoTemplate) :
    IMessageServiceOutPort, IMessageCreateOutPort {
    override fun findMessageById(id: String): Mono<Message> {
        return mongoTemplate.findById<MongoMessage>(id).map {
            it.toDomainEntity()
        }
    }

    override fun create(message: Message): Mono<Message> {
        return mongoTemplate.save(message.copy(id = null))
    }

    override fun deleteAll(): Mono<Unit> {
        return mongoTemplate.remove<MongoMessage>(Query())
            .thenReturn(Unit)
    }

    override fun update(id: String, message: Message): Mono<Message> {
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
        ).map {
            it.toDomainEntity()
        }
    }

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove<MongoMessage>(query)
            .thenReturn(Unit)
    }

    override fun findMessagesByIds(ids: List<String>): Flux<Message> {
        val query = Query.query(Criteria.where("_id").`in`(ids))
        return mongoTemplate.find<MongoMessage>(query).map { it.toDomainEntity() }
    }

    override fun deleteMessagesByIds(ids: List<String>): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`in`(ids))
        return mongoTemplate.remove<MongoMessage>(query)
            .thenReturn(Unit)
    }

    private fun MongoMessage.toDomainEntity(): Message {
        return Message(
            id = this.id,
            userId = this.userId,
            content = this.content,
            sentAt = this.sentAt
        )
    }
}
