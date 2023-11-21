package rys.ajaxpetproject.chat.application.mapper

import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent

fun Message.createEvent(chatId: String): MessageAddedEvent {
    val message = this@createEvent
    return MessageAddedEvent(chatId, message)
}
