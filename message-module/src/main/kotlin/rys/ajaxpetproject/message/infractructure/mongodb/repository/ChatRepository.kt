package rys.ajaxpetproject.message.infractructure.mongodb.repository

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import rys.ajaxpetproject.internalapi.mongodb.model.MongoChat
import rys.ajaxpetproject.message.application.port.`in`.IEventSubOutPort
import rys.ajaxpetproject.message.application.port.out.IMessageServiceOutPort
import rys.ajaxpetproject.message.domain.entity.Message

@Repository
class ChatRepository(private val mongoTemplate: ReactiveMongoTemplate,
    private val messageRepository: IMessageServiceOutPort
): IEventSubOutPort {
    override fun getMessagesInChat(chatId: String): Flux<Message> {
      return mongoTemplate.findById<MongoChat>(chatId)
          .flatMapMany { chat ->
              messageRepository.findMessagesByIds(chat.messages)
          }
    }
}
