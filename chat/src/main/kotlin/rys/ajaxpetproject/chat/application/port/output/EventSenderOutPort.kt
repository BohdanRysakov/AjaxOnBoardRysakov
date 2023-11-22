package rys.ajaxpetproject.chat.application.port.output

import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent

@Suppress("InvalidPackageDeclaration")
interface EventSenderOutPort {
    fun sendCreateEvent(event: MessageAddedEvent): Mono<Unit>
}
