package rys.ajaxpetproject.chat.infrastructure.adapter

import rys.ajaxpetproject.request.message.create.proto.CreateEvent

interface EventBroadcaster {
    fun handleEvent(event: CreateEvent.MessageCreatedEvent)
}
