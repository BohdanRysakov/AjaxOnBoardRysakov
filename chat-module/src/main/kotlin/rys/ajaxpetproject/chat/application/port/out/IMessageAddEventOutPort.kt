package rys.ajaxpetproject.chat.application.port.out

import reactor.core.publisher.Flux
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

interface IMessageAddEventOutPort {
    fun publishMessageCreatedEvent(chatId: String) : Flux<EventSubscription.CreateSubscriptionResponse>

    fun loadInitialState(chatId: String): Flux<EventSubscription.CreateSubscriptionResponse>
}
