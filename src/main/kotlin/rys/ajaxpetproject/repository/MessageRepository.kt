package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.MongoMessage

interface MessageRepository : MongoRepository<MongoMessage, ObjectId> {

    fun getMessageById(id:ObjectId):MongoMessage?

    fun findMessageById(id:ObjectId) : MongoMessage?

    fun findMessagesByChatId(chatId:ObjectId): List<MongoMessage>?

    fun getMessagesByChatId(chatId:ObjectId): List<MongoMessage>?

    fun deleteMessageById(id:ObjectId):Boolean

}
