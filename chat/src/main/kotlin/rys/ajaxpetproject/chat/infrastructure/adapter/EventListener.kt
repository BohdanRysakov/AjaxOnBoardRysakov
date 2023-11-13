package rys.ajaxpetproject.chat.infrastructure.adapter

import reactor.core.publisher.Flux
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

interface EventListener {
    fun catchMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse>
}
