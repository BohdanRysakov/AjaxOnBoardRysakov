package rys.ajaxpetproject.service.impl

import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.MessageNotFoundException
import rys.ajaxpetproject.exception.MessagesFromChatNotFoundException
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService

@Service
class MessageServiceImpl(val messageRepository: MessageRepository, val chatService: ChatService) : MessageService {
    override fun createMessage(mongoMessage: MongoMessage) = messageRepository.save(mongoMessage)

    override fun getMessageById(id: ObjectId) : MongoMessage =
        messageRepository.getMessageById(id) ?: throw MessageNotFoundException()

    override fun findMessageById(id: ObjectId): MongoMessage? = messageRepository.findMessageById(id)

    override fun getAllMessagesByChatId(chatId: ObjectId) : List<MongoMessage> =
        messageRepository.getMessagesByChatId(chatId)
        ?: throw MessagesFromChatNotFoundException()

    override fun findAllMessagesByChatId(chatId: ObjectId): List<MongoMessage>? {
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

    override fun deleteMessages() : Boolean = messageRepository.deleteAll()
}
