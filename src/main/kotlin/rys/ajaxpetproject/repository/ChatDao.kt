package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoChat

interface ChatDao {
    fun save(mongoChat: MongoChat): MongoChat

    fun findChatById(id: ObjectId): MongoChat?

    fun findAllChats(): List<MongoChat>

    fun deleteChatById(id: ObjectId): Boolean

    fun deleteAllChats(): Boolean
}
