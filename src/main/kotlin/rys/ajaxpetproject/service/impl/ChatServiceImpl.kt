package rys.ajaxpetproject.service.impl


import org.springframework.stereotype.Service
import rys.ajaxpetproject.exception.ChatNotFoundException
import rys.ajaxpetproject.exception.ChatsNotFoundException
import rys.ajaxpetproject.model.Chat
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.service.ChatService
import java.util.*

@Service
class ChatServiceImpl(val chatRepository: ChatRepository) : ChatService {
    override fun createChat(chat: Chat) = chatRepository.save(chat)
    override fun getChatById(id: UUID) = chatRepository.getChatById(id) ?: throw ChatNotFoundException()
    override fun findChatById(id: UUID): Chat? = chatRepository.findChatById(id)

    override fun getAllChats(): List<Chat> = chatRepository.findAllBy() ?: throw ChatsNotFoundException()
    override fun findAllChats(): List<Chat>?  = chatRepository.findAllBy() ?: emptyList()

    override fun updateChat(id: UUID, updatedChat: Chat) = getChatById(id).let { chatRepository.save(updatedChat) }
    override fun deleteChat(id: UUID) : Boolean {
        val chat = getChatById(id) // Checking that chat exists if not throws exception
        chatRepository.deleteById(id)
        return true
    }

    override fun deleteChats() = chatRepository.deleteAll()

}
