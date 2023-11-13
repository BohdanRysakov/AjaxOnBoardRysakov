package rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import rys.ajaxpetproject.chat.infrastructure.adapter.EventListener
import rys.ajaxpetproject.chat.application.port.out.MessageAddEventOutPort
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

@Service
class EventProcessService(
    private val eventPublisher: EventListener,
) : MessageAddEventOutPort {
    override fun publishMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse> {
        return eventPublisher.catchMessageCreatedEvent(chatId)
    }
}
