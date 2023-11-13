package rys.ajaxpetproject.chat.domain

import java.util.*

data class Chat(
    val id: String? = null,
    val name: String?,
    val users: List<String> = emptyList(),
    val messages: List<String> = emptyList()
)

data class Message(
    val id: String? = null,
    val userId: String,
    val content: String,
    val sentAt: Date? = null
)
