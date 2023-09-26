package rys.ajaxpetproject.repository.impl.mongoTemplate

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageDao

@Repository
class MessageRepository(private val mongoTemplate: MongoTemplate) : MessageDao {

    private val clazz = MongoMessage::class.java

    override fun save(mongoMessage: MongoMessage): MongoMessage = mongoTemplate.save(mongoMessage)

    override fun findMessageById(id: ObjectId): MongoMessage? = mongoTemplate.findById(id, clazz)

    override fun findMessagesByChatId(chatId: ObjectId): List<MongoMessage> =
        mongoTemplate.find(Query(Criteria.where("chatId").`is`(chatId)), clazz)

    override fun deleteMessageById(id: ObjectId): Boolean {
        val result = mongoTemplate.remove(Query(Criteria.where("_id").`is`(id)), clazz)
        return result.deletedCount > 0
    }

    override fun deleteAllMessages(): Boolean {
        mongoTemplate.remove(Query(), clazz)
        return true
    }
}
