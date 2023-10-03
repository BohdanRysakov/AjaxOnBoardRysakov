package rys.rest.service.impl

import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.rest.exceptions.MessageNotFoundException
import rys.rest.model.MongoMessage
import rys.rest.repository.MessageRepository
import rys.rest.service.ChatService
import rys.rest.service.MessageService

@Service
class MessageServiceImpl(val messageRepository: MessageRepository, val chatService: ChatService) : MessageService {
    override fun createMessage(mongoMessage: MongoMessage) = messageRepository.save(mongoMessage)

    override fun findMessageById(id: ObjectId): MongoMessage? = messageRepository.findMessageById(id)

    override fun findAllMessagesByChatId(chatId: ObjectId): List<MongoMessage> {
        chatService.findChatById(chatId)
        return messageRepository.findMessagesByChatId(chatId)
    }

    override fun updateMessage(id: ObjectId, updatedMongoMessage: MongoMessage) : MongoMessage =
        findMessageById(id)
            ?.let { messageRepository.save(updatedMongoMessage) }
            ?: throw MessageNotFoundException()

    override fun deleteMessage(id: ObjectId): Boolean =
        findMessageById(id)
            ?.let { messageRepository.deleteMessageById(id) }
            ?: throw MessageNotFoundException()

    override fun deleteMessages()  = messageRepository.deleteAll()
}
