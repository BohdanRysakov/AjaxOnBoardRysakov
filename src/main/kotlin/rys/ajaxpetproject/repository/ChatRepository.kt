package rys.ajaxpetproject.repository

import org.springframework.data.repository.CrudRepository
import rys.ajaxpetproject.model.Chat
import java.util.*

interface ChatRepository : CrudRepository<Chat, UUID> {
     fun findChatById(id:UUID) : Chat?
     fun getChatById(id:UUID) : Chat?
     fun findAllBy() : List<Chat>?
}