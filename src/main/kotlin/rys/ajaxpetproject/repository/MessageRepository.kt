package rys.ajaxpetproject.repository

import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.Message
import java.util.*

interface MessageRepository : MongoRepository<Message, UUID> {
    fun findByChatId(chatId: UUID): List<Message>
    fun getMessageById(id:UUID):Message?

    fun findMessageById(id:UUID) : Message?
    fun findMessagesByChatId(chatId:UUID): List<Message>?
    fun getMessagesByChatId(chatId:UUID): List<Message>?
}