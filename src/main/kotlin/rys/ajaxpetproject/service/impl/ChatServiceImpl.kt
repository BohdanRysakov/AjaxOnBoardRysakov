package rys.ajaxpetproject.service.impl


import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.ChatNotFoundException
import rys.ajaxpetproject.exception.ChatsNotFoundException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.service.ChatService

@Service
class ChatServiceImpl(val chatRepository: ChatRepository) : ChatService {
    override fun createChat(mongoChat: MongoChat) = chatRepository.save(mongoChat)
    override fun findChatById(id: ObjectId): MongoChat? = chatRepository.findChatById(id)

    fun getChatById(id: ObjectId) = chatRepository.findChatById(id) ?: throw ChatNotFoundException()


    fun getAllChats(): List<MongoChat> = chatRepository.findAllBy() ?: throw ChatsNotFoundException()
    override fun findAllChats(): List<MongoChat>?  = chatRepository.findAllBy() ?: emptyList()

     override fun updateChat(id: ObjectId, updatedMongoChat: MongoChat)
    = getChatById(id).let { chatRepository.save(updatedMongoChat) }
    override fun deleteChat(id: ObjectId) : Boolean {
        findChatById(id)?.let {
            chatRepository.deleteById(id)
            return true
        } ?: throw ChatNotFoundException()
    }

    override fun deleteChats() = chatRepository.deleteAll()

}
