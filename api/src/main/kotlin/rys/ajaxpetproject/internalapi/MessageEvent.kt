package rys.ajaxpetproject.internalapi

import rys.ajaxpetproject.internalapi.MessageDestinations.EVENT_PREFIX

object MessageEvent {
    const val MESSAGE_CREATE_EVENT = "$EVENT_PREFIX.message.message_create_event"

    fun createMessageCreateNatsSubject(chatId: String): String = "$MESSAGE_CREATE_EVENT.$chatId"
}
