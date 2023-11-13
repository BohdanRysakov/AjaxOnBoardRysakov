package rys.ajaxpetproject.chat.application.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.mapper.toDto
import rys.ajaxpetproject.chat.application.port.`in`.ChatServiceInPort
import rys.ajaxpetproject.chat.application.port.out.EventListenerOutPort
import rys.ajaxpetproject.chat.application.port.out.MessageAddEventOutPort
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

@Service
class EventProcessService(
    private val eventPublisher: EventListenerOutPort,
    private val chatService: ChatServiceInPort
) : MessageAddEventOutPort {
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
