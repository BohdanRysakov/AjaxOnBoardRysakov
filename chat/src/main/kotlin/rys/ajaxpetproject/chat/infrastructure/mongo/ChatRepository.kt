package rys.ajaxpetproject.chat.infrastructure.mongo

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.findAndModify
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import rys.ajaxpetproject.chat.application.port.out.ChatServiceOutPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.chat.infrastructure.mapper.toDomainChat
import rys.ajaxpetproject.chat.infrastructure.mapper.toDomainMessage
import rys.ajaxpetproject.chat.infrastructure.mapper.toMongoChat
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.MessageRepository

@Repository
@Suppress("TooManyFunctions")
class ChatRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: MessageRepository
) : ChatServiceOutPort {

    override fun findChatById(id: String): Mono<Chat> {
        return mongoTemplate.findById<MongoChat>(id).map { it.toDomainChat() }
    }

    override fun save(chat: Chat): Mono<Chat> {
        return mongoTemplate.save(chat.toMongoChat()).map { it.toDomainChat() }
    }

    override fun deleteAll(): Mono<Unit> {
        return mongoTemplate.remove<MongoChat>(Query())
            .thenReturn(Unit)
    }

    override fun update(id: String, chat: Chat): Mono<Chat> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        val updateDef = Update()
            .set("name", chat.name)
            .set("users", chat.users)
            .set("messages", chat.messages)

        return mongoTemplate.findAndModify<MongoChat>(
            query,
            updateDef,
            FindAndModifyOptions.options().returnNew(true)
        ).map { it.toDomainChat() }
    }

    override fun addUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().addToSet("users", userId)
        return mongoTemplate.findAndModify(
            query,
            updateDef,
            MongoChat::class.java
        )
            .switchIfEmpty {
                Mono.error(IllegalArgumentException("Chat with id $chatId not found"))
            }
            .thenReturn(Unit)
    }

    override fun removeUser(userId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().pull("users", userId)

        return mongoTemplate.findAndModify(
            query,
            updateDef,
            MongoChat::class.java
        )
            .thenReturn(Unit)
    }

    override fun addMessage(messageId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().addToSet("messages", messageId)

        return mongoTemplate.findAndModify(
            query,
            updateDef,
            MongoChat::class.java
        )
            .switchIfEmpty {
                Mono.error(IllegalArgumentException("Chat with id $chatId not found"))
            }
            .thenReturn(Unit)
    }

    override fun removeMessage(messageId: String, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().pull("messages", messageId)
        return mongoTemplate.findAndModify(
            query,
            updateDef,
            MongoChat::class.java
        )
            .thenReturn(Unit)
    }

    override fun removeMessages(ids: List<String>, chatId: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(chatId))
        val updateDef = Update().pullAll("messages", ids.toTypedArray())
        return mongoTemplate.findAndModify(
            query,
            updateDef,
            MongoChat::class.java
        )
            .thenReturn(Unit)
    }

    override fun delete(id: String): Mono<Unit> {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove<MongoChat>(query)
            .thenReturn(Unit)
    }

    override fun findAll(): Flux<Chat> {
        return mongoTemplate.findAll<MongoChat>().map { it.toDomainChat() }
    }

    override fun findChatsByUserId(userId: String): Flux<Chat> {
        val query = Query.query(Criteria.where("users").`is`(userId))
        return mongoTemplate.find<MongoChat>(query).map { it.toDomainChat() }
    }

    override fun findMessagesByUserIdAndChatId(userId: String, chatId: String): Flux<Message> {
        val query = Query.query(Criteria.where("_id").`is`(chatId).and("users").`is`(userId))
        return mongoTemplate.findOne<MongoChat>(query)
            .flatMapMany { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }
            .filter { it.userId == userId }.map { it.toDomainMessage() }
    }

    override fun findMessagesFromChat(chatId: String): Flux<Message> {
        return findChatById(chatId)
            .flatMapMany { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }.map { it.toDomainMessage() }
    }

    override fun deleteMessagesFromChatByUserId(chatId: String, userId: String): Mono<Unit> {
        return findChatById(chatId)
            .flatMapMany { chat ->
                messageRepository.findMessagesByIds(chat.messages)
            }
            .filter { it.userId == userId }
            .map { it.id!! }
            .collectList()
            .flatMap { messageIds ->
                messageRepository.deleteMessagesByIds(messageIds).thenReturn(messageIds)
            }
            .flatMap { messageIds ->
                removeMessages(messageIds, chatId)
            }
    }
}
