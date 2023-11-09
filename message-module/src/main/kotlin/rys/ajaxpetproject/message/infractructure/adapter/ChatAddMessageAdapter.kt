package rys.ajaxpetproject.message.infractructure.adapter

import reactor.core.publisher.Mono

interface ChatAddMessageAdapter {
    fun addMessage(chatId: String, messageId: String) : Mono<Unit>
}
