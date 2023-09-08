package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoMessage

interface MessageService {

    fun createMessage(mongoMessage: MongoMessage): MongoMessage

    fun findMessageById(id: ObjectId): MongoMessage?

    fun findAllMessagesByChatId(chatId: ObjectId): List<MongoMessage>

    fun updateMessage(id: ObjectId, updatedMongoMessage: MongoMessage): MongoMessage?

    fun deleteMessage(id: ObjectId): Boolean

    fun deleteMessages()
}
