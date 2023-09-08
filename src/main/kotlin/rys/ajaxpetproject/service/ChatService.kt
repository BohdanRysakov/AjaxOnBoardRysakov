package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoChat
interface ChatService {
    fun createChat(mongoChat: MongoChat): MongoChat

    fun findChatById(id: ObjectId): MongoChat?

    fun findAllChats() : List<MongoChat>?

    fun updateChat(id: ObjectId, updatedMongoChat: MongoChat): MongoChat?

    fun deleteChat(id: ObjectId): Boolean

    fun deleteChats()

}
