package rys.ajaxpetproject.chat.domain.event

import rys.ajaxpetproject.chat.domain.entity.Message

class MessageAddEvent(
    val chatId: String,
    val message : Message
)
