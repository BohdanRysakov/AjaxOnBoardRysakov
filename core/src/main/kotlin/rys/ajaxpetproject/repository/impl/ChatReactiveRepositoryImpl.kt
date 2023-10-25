package rys.ajaxpetproject.repository.impl

import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.repository.MessageRepository

@Repository
@Suppress("TooManyFunctions")
class ChatReactiveRepositoryImpl(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: MessageRepository
) : ChatRepository {

    override fun findChatById(id: String): Mono<MongoChat> {
        return mongoTemplate.findById<MongoChat>(id)
    }

    override fun save(chat: MongoChat): Mono<MongoChat> {
        return mongoTemplate.save(chat)
    }

    override fun deleteAll(): Mono<Unit> {
        return mongoTemplate.remove<MongoChat>(Query())
            .doOnSuccess { }
            .thenReturn(Unit)
    }

    override fun update(id: String, chat: MongoChat): Mono<MongoChat> {
        val query = Query.query(Criteria.where("_id").`is`(id))
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

    override fun addUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().addToSet("users", userId)
        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
            .thenReturn(Unit)
    }

    override fun removeUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().pull("users", userId)

        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
            .thenReturn(Unit)
    }

    override fun addMessage(messageId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().addToSet("messages", messageId)

        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
            .thenReturn(Unit)
    }

    override fun removeMessage(messageId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().pull("messages", messageId)
        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
            .thenReturn(Unit)
    }

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove<MongoChat>(query)
            .thenReturn(Unit)
    }

    override fun findAll(): Flux<MongoChat> {
        return mongoTemplate.findAll<MongoChat>()
    }

    override fun findChatsByUserId(userId: String): Flux<MongoChat> {
        val query = Query.query(Criteria.where("users").`is`(userId))
        return mongoTemplate.find<MongoChat>(query)
    }

    override fun findMessagesByUserIdAndChatId(userId: String, chatId: String): Flux<MongoMessage> {
        val query = Query.query(Criteria.where("_id").`is`(chatId).and("users").`is`(userId))
        return mongoTemplate.find<MongoChat>(query)
            .flatMap { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }
            .filter { it.userId == userId }
    }

    override fun findMessagesFromChat(chatId: String): Flux<MongoMessage> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        return mongoTemplate.findOne<MongoChat>(query)
            .flatMapMany { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }
    }

    override fun deleteMessagesFromUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        return mongoTemplate.findOne<MongoChat>(query)
            .flatMap { chat ->
                messageRepository.deleteMessagesByIds(chat.messages.filter { it == userId })
            }
            .thenReturn(Unit)
    }
}
