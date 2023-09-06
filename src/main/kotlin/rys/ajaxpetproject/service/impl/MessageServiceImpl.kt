package rys.ajaxpetproject.service.impl

import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.MessageNotFoundException
import rys.ajaxpetproject.exception.MessagesFromChatNotFoundException
import rys.ajaxpetproject.model.Message
import rys.ajaxpetproject.repository.MessageRepository
import rys.ajaxpetproject.service.MessageService
import java.util.*

@Service
class MessageServiceImpl(val messageRepository: MessageRepository, val chatService : ChatServiceImpl) : MessageService {
    override fun createMessage(message: Message) = messageRepository.save(message)
    override fun getMessageById(id: UUID)  = messageRepository.getMessageById(id) ?: throw MessageNotFoundException()
    override fun findMessageById(id: UUID): Message? = messageRepository.findMessageById(id)

    override fun getAllMessagesByChatId(chatId: UUID)  = messageRepository.getMessagesByChatId(chatId)
        ?: throw MessagesFromChatNotFoundException()
    override fun findAllMessagesByChatId(chatId: UUID): List<Message>?  {
        chatService.findChatById(chatId)
        return messageRepository.findMessagesByChatId(chatId)
    }

    override fun updateMessage(id: UUID, updatedMessage: Message) =
        getMessageById(id).let { messageRepository.save(updatedMessage) } //todo exception
    override fun deleteMessage(id: UUID) = messageRepository.deleteById(id).let { true } //todo exception

    override fun deleteMessages() = messageRepository.deleteAll()

}

