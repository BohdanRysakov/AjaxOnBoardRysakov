package rys.ajaxpetproject.repository.impl

import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Operators.`as`
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
        val query = Query.query(Criteria.where("_id").`is`(ObjectId(id)))
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

    override fun update(id: String, chat: MongoChat): Mono<MongoChat> {
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

    override fun addUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("id").`is`(chatId))
        val updateDef = Update().addToSet("users", userId)
        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
            .thenReturn(Unit)
    }

    override fun removeUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("id").`is`(chatId))
        val updateDef = Update().pull("users", userId)
        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        )
            .thenReturn(Unit)
    }

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("id").`is`(id))
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
        val matchStage = MatchOperation(Criteria.where("id").`is`(ObjectId(chatId)))

        // Lookup stage
        val lookupStage = LookupOperation.newLookup()
            .from("MESSAGES")
            .localField("messages")
            .foreignField("id")
            .`as`("usersMessages")

        // Filter stage
        val filterStage = Aggregation.project()
            .and(
                Filter.filter("usersMessages")
                    .`as`("message")
                    .by(
                        ComparisonOperators.Eq.valueOf("message.userId")
                            .equalToValue(userId)
                    )
            )
            .`as`("messages")

        val aggregation = Aggregation.newAggregation(matchStage, lookupStage, filterStage)
        return mongoTemplate
            .aggregate(aggregation, "CHATS", MongoMessage::class.java)

    }


    override fun findMessagesFromChat(chatId: String): Flux<MongoMessage> {
        val query = Query.query(Criteria.where("id").`is`(chatId))
        return mongoTemplate.findOne<MongoChat>(query)
            .flatMapMany { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }
    }

    override fun deleteMessagesFromUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("id").`is`(chatId))
        return mongoTemplate.findOne<MongoChat>(query)
            .flatMap { chat ->
                messageRepository.deleteMessagesByIds(chat.messages.filter { it == userId })
            }
            .doOnSuccess { }
            .thenReturn(Unit)
    }


}
