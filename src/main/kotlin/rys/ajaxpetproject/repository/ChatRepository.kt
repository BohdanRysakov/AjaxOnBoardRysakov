package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rys.ajaxpetproject.model.MongoChat

@Repository
interface ChatRepository : MongoRepository<MongoChat, ObjectId> {
     fun findChatById(id:ObjectId) : MongoChat?
     fun findAllBy() : List<MongoChat>?
}
