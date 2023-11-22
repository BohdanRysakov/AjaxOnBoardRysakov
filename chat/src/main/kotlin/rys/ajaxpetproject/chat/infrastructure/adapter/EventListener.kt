package rys.ajaxpetproject.chat.infrastructure.adapter

import reactor.core.publisher.Flux
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent

interface EventListener {
    fun handleMessageCreatedEvent(chatId: String):
        Flux<MessageAddedEvent>
}
