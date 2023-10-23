package rys.ajaxpetproject.repository.impl

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.repository.MessageRepository

class ChatReactiveRepositoryImpl(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: MessageRepository
) : ChatRepository {

    override fun findChatById(id: ObjectId): Mono<MongoChat> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.findById<MongoChat>(query)
    }

    override fun save(chat: MongoChat): Mono<MongoChat> {
        return mongoTemplate.save(chat)
    }

    override fun deleteAll(): Mono<Unit> {
        return mongoTemplate.remove<MongoChat>(Query())
            .doOnSuccess { }
            .thenReturn(Unit)
    }

    override fun update(id: ObjectId, chat: MongoChat): Mono<MongoChat> {
        val query = Query.query(Criteria.where("id").`is`(id))
        val updateDef = Update()
            .set("name", chat.name)
            .set("users", chat.users)
            .set("messages", chat.messages)

        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
    }

    override fun delete(id: ObjectId): Mono<Unit> {
        val query = Query.query(Criteria.where("id").`is`(id))
        return mongoTemplate.remove<MongoChat>(query)
            .doOnSuccess { }
            .thenReturn(Unit)
    }

    override fun findAll(): Flux<MongoChat> {
        return mongoTemplate.findAll<MongoChat>()
    }

    override fun findChatsByUserId(userId: ObjectId): Flux<MongoChat> {
        val query = Query.query(Criteria.where("users").`is`(userId))
        return mongoTemplate.find<MongoChat>(query)
    }

    override fun findMessagesByUserIdAndChatId(userId: ObjectId, chatId: ObjectId): Flux<MongoMessage> {
        val query = Query.query(Criteria.where("users").`is`(userId).and("id").`is`(chatId))
        return mongoTemplate.findOne<MongoChat>(query)
            .flatMapMany { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }
            .filter { it.userId == userId }
    }
}
