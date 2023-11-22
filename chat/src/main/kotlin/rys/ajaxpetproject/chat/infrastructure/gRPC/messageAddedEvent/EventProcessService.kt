package rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import rys.ajaxpetproject.chat.infrastructure.adapter.EventListener
import rys.ajaxpetproject.chat.application.port.output.MessageAddEventOutPort
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent

@Service
class EventProcessService(
    private val eventPublisher: EventListener,
) : MessageAddEventOutPort {
    override fun publishMessageCreatedEvent(chatId: String):
            Flux<MessageAddedEvent> {
        return eventPublisher.handleMessageCreatedEvent(chatId)
    }
}
