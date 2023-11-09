package rys.ajaxpetproject.chat.infractructure.mongodb.repository

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.application.port.out.IMessageServiceOutPort
import rys.ajaxpetproject.chat.domain.entity.Message
import rys.ajaxpetproject.internalapi.mongodb.model.MongoMessage

@Repository
class MessageRepository(private val mongoTemplate: ReactiveMongoTemplate) : IMessageServiceOutPort {
    override fun getMessageById(id: String): Mono<Message> {
        return mongoTemplate.findById<MongoMessage>(id).map { it.toDomainMessage() }
    }

    override fun getMessagesByIds(ids: List<String>): Flux<Message> {
        val query = Query.query(Criteria.where("_id").`in`(ids))
        return mongoTemplate.find<MongoMessage>(query).map { it.toDomainMessage() }


    }

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove<MongoMessage>(query)
            .thenReturn(Unit)
    }

    private fun MongoMessage.toDomainMessage(): Message {
        return Message(
            id = this.id!!,
            userId = this.userId,
            content = this.content,
            sentAt = this.sentAt
        )
    }
}
