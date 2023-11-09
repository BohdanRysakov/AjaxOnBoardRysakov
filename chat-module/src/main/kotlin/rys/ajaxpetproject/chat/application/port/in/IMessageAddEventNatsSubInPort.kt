package rys.ajaxpetproject.chat.application.port.`in`

import reactor.core.publisher.Flux
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

interface IMessageAddEventNatsSubInPort {
    fun catchMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse>
}
