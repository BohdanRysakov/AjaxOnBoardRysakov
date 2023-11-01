package rys.ajaxpetproject.internalapi

import rys.ajaxpetproject.internalapi.MessageDestinations.REQUEST_PREFIX

object ChatSubjectsV1 {

    object ChatRequest {
        private const val CHAT_REQUEST = "$REQUEST_PREFIX.chat"

        const val CREATE = "$CHAT_REQUEST.create"
        const val FIND_ONE = "$CHAT_REQUEST.find_one"
        const val FIND_ALL = "$CHAT_REQUEST.find_all"
        const val UPDATE = "$CHAT_REQUEST.update"
        const val DELETE = "$CHAT_REQUEST.delete"
    }

}
