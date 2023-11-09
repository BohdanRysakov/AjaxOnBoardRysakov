package rys.ajaxpetproject.chat.application.port.`in`

import reactor.core.publisher.Flux
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

interface IMessageAddedEventPublishInPort {
    fun publishMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse>

    fun loadInitialState(chatId: String): Flux<EventSubscription.CreateSubscriptionResponse>
}
