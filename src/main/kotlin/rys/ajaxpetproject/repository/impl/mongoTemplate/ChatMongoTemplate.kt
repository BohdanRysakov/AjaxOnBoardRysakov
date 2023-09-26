package rys.ajaxpetproject.repository.impl.mongoTemplate

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.ChatDAO

@Repository
class ChatMongoTemplate(private val mongoTemplate: MongoTemplate) : ChatDAO {

    private val clazz = MongoChat::class.java

    override fun save(mongoChat: MongoChat): MongoChat = mongoTemplate.save(mongoChat)

    override fun getChatById(id: ObjectId): MongoChat? = mongoTemplate.findById(id, clazz)

    override fun findAllChats(): List<MongoChat> = mongoTemplate.findAll(clazz)

    override fun deleteChatById(id: ObjectId): Boolean {
        val result = mongoTemplate.remove(Query(Criteria.where("_id").`is`(id)), clazz)
        return result.deletedCount > 0
    }

    override fun deleteAllChats(): Boolean {
        mongoTemplate.dropCollection(clazz)
        return true
    }
}
