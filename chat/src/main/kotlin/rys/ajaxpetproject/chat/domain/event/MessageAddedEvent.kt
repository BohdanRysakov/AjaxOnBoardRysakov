package rys.ajaxpetproject.chat.domain.event

import rys.ajaxpetproject.chat.domain.Message

class MessageAddedEvent(
    val chatId: String,
    val message : Message
)
