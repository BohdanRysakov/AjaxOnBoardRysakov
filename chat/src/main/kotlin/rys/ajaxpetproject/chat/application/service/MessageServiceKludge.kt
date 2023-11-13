package rys.ajaxpetproject.chat.application.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.chat.application.port.out.MessageServiceOutPort
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.service.MessageService

@Service
class MessageServiceKludge(
    private val messageService: MessageService
) : MessageServiceOutPort {
    override fun findMessageById(id: String): Mono<Message> {
        return messageService.findMessageById(id).map { it.toValueObject() }
    }

    override fun getMessageById(id: String): Mono<Message> {
        return messageService.getMessageById(id).map { it.toValueObject() }
    }

    override fun create(message: Message): Mono<Message> {
        return messageService.create(message.toMongoMessage()).map { it.toValueObject() }
    }

    override fun deleteAll(): Mono<Unit> {
        return messageService.deleteAll()
    }

    override fun update(id: String, message: Message): Mono<Message> {
        return messageService.update(id, message.toMongoMessage()).map { it.toValueObject() }
    }

    override fun delete(id: String): Mono<Unit> {
        return messageService.delete(id)
    }

    override fun findMessagesByIds(ids: List<String>): Flux<Message> {
        return messageService.findMessagesByIds(ids).map { it.toValueObject() }
    }

    override fun deleteMessagesByIds(ids: List<String>): Mono<Unit> {
        return messageService.deleteMessagesByIds(ids)
    }

    internal fun MongoMessage.toValueObject(): Message {
        return Message(
            id = this.id,
            userId = this.userId,
            content = this.content,
            sentAt = this.sentAt
        )
    }

    internal fun Message.toMongoMessage(): MongoMessage {
        return MongoMessage(
            id = this.id,
            userId = this.userId,
            content = this.content,
        )
    }
}
