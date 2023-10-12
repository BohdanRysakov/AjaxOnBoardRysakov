package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.ajaxpetproject.exceptions.ChatNotFoundException
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.service.ChatService

@Service
class ChatServiceImpl(val chatRepository: ChatRepository) : ChatService {

    override fun createChat(mongoChat: MongoChat): MongoChat {
        return chatRepository.save(mongoChat)
    }

    override fun findChatById(id: ObjectId): MongoChat? = chatRepository.findChatById(id)

    override fun findAllChats(): List<MongoChat> = chatRepository.findAllBy()

    override fun updateChat(id: ObjectId, updatedMongoChat: MongoChat): MongoChat {
        return findChatById(id)
            .let {
                chatRepository.save(updatedMongoChat.copy(id = id))
            }
    }

    override fun deleteChat(id: ObjectId): Boolean {
        findChatById(id)?.let {
            chatRepository.deleteById(id)
            return true
        } ?: throw ChatNotFoundException("Chat not found")
    }

    override fun deleteChats() = chatRepository.deleteAll()
}
