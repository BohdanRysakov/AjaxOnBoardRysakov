package rys.ajaxpetproject.chat.domain.event

import rys.ajaxpetproject.chat.domain.Message

class MessageAddEvent(
    val chatId: String,
    val message : Message
)
