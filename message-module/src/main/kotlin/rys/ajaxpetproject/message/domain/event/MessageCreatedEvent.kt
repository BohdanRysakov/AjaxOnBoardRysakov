package rys.ajaxpetproject.message.domain.event

import rys.ajaxpetproject.message.domain.entity.Message

class MessageCreatedEvent(
    val chatId : String,
    val message: Message
)
