package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoMessage

interface MessageDao {
    fun save(mongoMessage: MongoMessage): MongoMessage

    fun findMessageById(id: ObjectId): MongoMessage?

    fun findMessagesByChatId(chatId: ObjectId): List<MongoMessage>

    fun deleteMessageById(id: ObjectId): Boolean

    fun deleteAllMessages(): Boolean
}
