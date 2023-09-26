package rys.ajaxpetproject.repository.impl.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.ChatDAO

//@Repository
interface ChatRepository : MongoRepository<MongoChat, ObjectId>, ChatDAO {
    override fun getChatById(id: ObjectId): MongoChat?
    override fun findAllChats(): List<MongoChat>


}
