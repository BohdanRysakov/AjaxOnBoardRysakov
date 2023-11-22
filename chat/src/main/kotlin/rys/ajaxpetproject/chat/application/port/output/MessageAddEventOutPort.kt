package rys.ajaxpetproject.chat.application.port.output

import reactor.core.publisher.Flux
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent

interface MessageAddEventOutPort {
    fun publishMessageCreatedEvent(chatId: String): Flux<MessageAddedEvent>
}
