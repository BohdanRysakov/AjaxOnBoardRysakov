package rys.ajaxpetproject.message.application.port.`in`

import reactor.core.publisher.Flux
import rys.ajaxpetproject.message.domain.entity.Message

interface IEventSubOutPort {
    fun getMessagesInChat(chatId: String): Flux<Message>

}
