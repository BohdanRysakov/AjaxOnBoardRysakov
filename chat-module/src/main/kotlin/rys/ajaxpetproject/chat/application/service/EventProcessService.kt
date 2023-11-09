package rys.ajaxpetproject.chat.application.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.port.`in`.IChatServiceInPort
import rys.ajaxpetproject.chat.application.port.`in`.IMessageAddEventNatsSubInPort
import rys.ajaxpetproject.chat.application.port.out.IMessageAddEventOutPort
import rys.ajaxpetproject.chat.domain.entity.toDto
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

@Service
class EventProcessService(
    private val eventPublisher: IMessageAddEventNatsSubInPort,
    private val chatService: IChatServiceInPort
) : IMessageAddEventOutPort {
    override fun publishMessageCreatedEvent(chatId: String):
            Flux<EventSubscription.CreateSubscriptionResponse> {
        return eventPublisher.catchMessageCreatedEvent(chatId)
    }

    override fun loadInitialState(chatId: String): Flux<EventSubscription.CreateSubscriptionResponse> {
        return chatService.getMessagesInChat(chatId)
            .flatMap { message ->
                val messageDto = message.toDto(chatId)
                val response = buildSuccessResponse(messageDto)
                response.toMono()
            }
    }

    private fun buildSuccessResponse(messageDto: MessageDto):
            EventSubscription.CreateSubscriptionResponse {
        return EventSubscription.CreateSubscriptionResponse.newBuilder().apply {
            successBuilder.messageDto = messageDto
        }.build()
    }
}
