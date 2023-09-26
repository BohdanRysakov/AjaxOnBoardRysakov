package rys.ajaxpetproject.repository.impl.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.ChatDao

interface ChatRepository : MongoRepository<MongoChat, ObjectId>, ChatDao {

    override fun findChatById(id: ObjectId): MongoChat?

    override fun findAllChats(): List<MongoChat>

}
