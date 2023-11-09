package rys.ajaxpetproject.chat.infractructure.adapter

import rys.ajaxpetproject.request.message.create.proto.CreateEvent

interface EventPublisher {
    fun handleEvent(event: CreateEvent.MessageCreatedEvent)
}
