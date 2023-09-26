package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoMessage

interface MessageDAO {
    fun save(mongoMessage: MongoMessage): MongoMessage

    fun findMessageById(id: ObjectId): MongoMessage?

    fun getMessagesByChatId(chatId: ObjectId): List<MongoMessage>

    fun deleteMessageById(id: ObjectId): Boolean

    fun deleteAllMessages(): Boolean
}
