package rys.ajaxpetproject.chat.application.port.out

import reactor.core.publisher.Mono
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

@Suppress("InvalidPackageDeclaration")
interface EventSenderOutPort {
    fun sendCreateEvent(event: CreateEvent.MessageCreatedEvent): Mono<Unit>
}
