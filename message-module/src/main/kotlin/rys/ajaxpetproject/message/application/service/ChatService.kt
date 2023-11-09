package rys.ajaxpetproject.message.application.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.message.application.port.`in`.IEventSubInPort
import rys.ajaxpetproject.message.application.port.`in`.IEventSubOutPort
import rys.ajaxpetproject.message.domain.entity.Message
import rys.ajaxpetproject.message.infractructure.adapter.ChatAddMessageAdapter

@Service
class ChatService(private val chatRepository: IEventSubOutPort) :
    IEventSubInPort, ChatAddMessageAdapter {
    override fun getMessagesInChat(chatId: String): Flux<Message> {
        return chatRepository.getMessagesInChat(chatId)
    }

    override fun addMessage(chatId: String, messageId: String): Mono<Unit> {
        return Mono.`when`(
            getChatById(chatId),
            messageService.getMessageById(messageId)
        )
            .then(chatRepository.addMessage(messageId, chatId))
            .flatMap<Unit> {
                messageService.getMessageById(messageId)
                    .flatMap {
                        kafkaEventSender.sendCreateEvent(
                            it.createEvent(chatId)
                        )
                    }
            }
            .onErrorResume {
                logger.error("Error while adding message to chat: {}", it.message, it)
                Mono.error(it)
            }
            .thenReturn(Unit)
    }
}
