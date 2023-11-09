package rys.ajaxpetproject.chat.application.port.`in`

import reactor.core.publisher.Mono
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

interface IMessageAddEventInPort {
    fun sendCreateEvent(event: CreateEvent.MessageCreatedEvent): Mono<Unit>
}
