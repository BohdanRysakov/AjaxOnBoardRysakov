package rys.ajaxpetproject.chat.application.port.out

import reactor.core.publisher.Mono
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

interface IChatEventPublisherOutPort {
    fun sendEvent(event: CreateEvent.MessageCreatedEvent): Mono<Unit>
}
