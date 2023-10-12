package rys.ajaxpetproject.subjects

object ChatSubjectsV1 {
    private const val REQUEST_PREFIX = "v1.rys.ajaxpetproject.core.input.reqreply"

    object ChatRequest {
        private const val CHAT_REQUEST = "$REQUEST_PREFIX.chat"

        const val CREATE = "$CHAT_REQUEST.create"
        const val FIND_ONE = "$CHAT_REQUEST.find_one"
        const val FIND_ALL = "$CHAT_REQUEST.find_all"
        const val UPDATE = "$CHAT_REQUEST.update"
        const val DELETE = "$CHAT_REQUEST.delete"
    }

}
