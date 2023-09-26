package rys.ajaxpetproject.repository.impl.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageDao

interface MessageRepository : MongoRepository<MongoMessage, ObjectId>, MessageDao {

    override fun findMessageById(id: ObjectId): MongoMessage?

    override fun findMessagesByChatId(chatId: ObjectId): List<MongoMessage>

    override fun deleteMessageById(id: ObjectId): Boolean
}
