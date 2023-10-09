package rys.rest.service.impl


import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.rest.exceptions.ChatNotFoundException
import rys.rest.model.MongoChat
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService

@Service
class ChatServiceImpl(val chatRepository: ChatRepository): ChatService {

    override fun createChat(mongoChat: MongoChat): MongoChat {
        return chatRepository.save(mongoChat)
    }

    override fun findChatById(id: ObjectId): MongoChat? = chatRepository.findChatById(id)

    override fun findAllChats(): List<MongoChat> = chatRepository.findAllBy()

    override fun updateChat(id: ObjectId, updatedMongoChat: MongoChat): MongoChat =
        findChatById(id)
            .let { chatRepository.save(updatedMongoChat) }

    override fun deleteChat(id: ObjectId): Boolean {
        findChatById(id)?.let {
            chatRepository.deleteById(id)
            return true
        } ?: throw ChatNotFoundException("Chat not found")
    }

    override fun deleteChats() = chatRepository.deleteAll()
}
