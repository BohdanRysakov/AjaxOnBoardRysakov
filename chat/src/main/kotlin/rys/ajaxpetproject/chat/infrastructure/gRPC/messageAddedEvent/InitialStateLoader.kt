package rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.chat.application.mapper.toDto
import rys.ajaxpetproject.chat.application.port.`in`.ChatServiceInPort
import rys.ajaxpetproject.chat.infrastructure.adapter.InitialStateEventLoader
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription

@Component
class InitialStateLoader(
    private val chatService: ChatServiceInPort
) : InitialStateEventLoader {
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
