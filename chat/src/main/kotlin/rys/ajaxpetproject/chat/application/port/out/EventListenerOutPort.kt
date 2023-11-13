package rys.ajaxpetproject.chat.application.port.out

import reactor.core.publisher.Flux
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

interface EventListenerOutPort {
    fun catchMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse>
}
