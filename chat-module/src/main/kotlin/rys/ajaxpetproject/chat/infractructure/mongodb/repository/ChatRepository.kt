package rys.ajaxpetproject.chat.infractructure.mongodb.repository

import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.`in`.IMessageServiceInPort
import rys.ajaxpetproject.chat.application.port.out.IChatServiceOutPort
import rys.ajaxpetproject.chat.domain.entity.Chat
import rys.ajaxpetproject.chat.domain.entity.Message
import rys.ajaxpetproject.internalapi.mongodb.model.MongoChat
import rys.ajaxpetproject.internalapi.mongodb.model.MongoMessage
import rys.ajaxpetproject.service.UserService

@Repository
class ChatRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val messageService: IMessageServiceInPort,
    private val userService: UserService
) : IChatServiceOutPort {
    override fun findChatById(id: String): Mono<Chat> {
        return mongoTemplate.findById<MongoChat>(id).map { it.toDomainChat() }
    }

    override fun getChatById(id: String): Mono<Chat> {
        return findChatById(id)
            .switchIfEmpty {
                Mono.error(IllegalArgumentException("Chat with id $id not found"))
            }
    }

    override fun save(chat: Chat): Mono<Chat> {
        return mongoTemplate.save(chat)
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

    override fun getMessagesFromChatByUser(userId: String, chatId: String): Flux<Message> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .thenMany(
                getMessagesInChat(chatId).filter { it.userId == userId }
            )
    }

    override fun getMessagesInChat(chatId: String): Flux<Message> {
        return getChatById(chatId)
            .flatMapMany {
                messageService.getMessagesByIds(it.messages)
            }
    }

    override fun deleteAllFromUser(userId: String, chatId: String): Mono<Unit> {
        return Mono.`when`(
            userService.getUserById(userId),
            getChatById(chatId)
        )
            .then(
                getMessagesInChat(chatId)
                    .filter { it.userId == userId }
                    .mapNotNull { it.id }
                    .map { messageService.delete(it.toString()) }
                    .then(Unit.toMono())
            )
    }

    private fun MongoChat.toDomainChat(): Chat {
        return Chat(
            id = this.id!!,
            name = this.name,
            users = this.users,
            messages = this.messages
        )
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
