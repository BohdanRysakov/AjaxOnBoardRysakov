package rys.ajaxpetproject.repository.impl.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageDAO

//@Repository
interface MessageRepository : MongoRepository<MongoMessage, ObjectId>, MessageDAO {

    override fun findMessageById(id: ObjectId): MongoMessage?

    override fun getMessagesByChatId(chatId: ObjectId): List<MongoMessage>

    override fun deleteMessageById(id: ObjectId): Boolean
}
