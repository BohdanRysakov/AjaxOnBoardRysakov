package rys.ajaxpetproject.service.impl


import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.ChatNotFoundException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.ChatDAO
import rys.ajaxpetproject.service.ChatService

@Service
class ChatServiceImpl(private val chatRepository: ChatDAO) : ChatService {

    override fun createChat(mongoChat: MongoChat): MongoChat = chatRepository.save(mongoChat)

    override fun findChatById(id: ObjectId): MongoChat? = chatRepository.getChatById(id)

    override fun findAllChats(): List<MongoChat> = chatRepository.findAllChats()

    override fun updateChat(id: ObjectId, updatedMongoChat: MongoChat): MongoChat =
        findChatById(id)
            .let { chatRepository.save(updatedMongoChat) }

    override fun deleteChatById(id: ObjectId): Boolean {
        findChatById(id)?.let {
            chatRepository.deleteChatById(id)
            return true
        } ?: throw ChatNotFoundException()
    }

    override fun deleteAllChats() = chatRepository.deleteAllChats()
}
