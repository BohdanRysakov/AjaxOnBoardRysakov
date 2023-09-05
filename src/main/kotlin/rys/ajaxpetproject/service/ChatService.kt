package rys.ajaxpetproject.service

import rys.ajaxpetproject.exception.ChatNotFoundException
import rys.ajaxpetproject.model.Chat
import java.util.*
import kotlin.jvm.Throws

interface ChatService {
    fun createChat(chat: Chat): Chat?
    @Throws(ChatNotFoundException::class)
    fun getChatById(id: UUID): Chat

    fun findChatById(id: UUID): Chat?
    @Throws(ChatNotFoundException::class)
    fun getAllChats(): List<Chat>
    fun findAllChats() : List<Chat>?
    fun updateChat(id: UUID, updatedChat: Chat): Chat?
    fun deleteChat(id: UUID): Boolean
}
