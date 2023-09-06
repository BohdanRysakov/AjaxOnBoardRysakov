package rys.ajaxpetproject.service

import rys.ajaxpetproject.exception.MessageNotFoundException
import rys.ajaxpetproject.model.Message
import java.util.*
import kotlin.jvm.Throws

interface MessageService {
    fun createMessage(message: Message): Message
    @Throws(MessageNotFoundException::class)
    fun getMessageById(id: UUID): Message
    fun findMessageById(id:UUID):Message?
    @Throws(MessageNotFoundException::class)
    fun getAllMessagesByChatId(chatId: UUID): List<Message>
    fun findAllMessagesByChatId(chatId: UUID): List<Message>?
    fun updateMessage(id: UUID, updatedMessage: Message): Message?
    fun deleteMessage(id: UUID): Boolean
    fun deleteMessages()
}
