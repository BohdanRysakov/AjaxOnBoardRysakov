package rys.ajaxpetproject.chat.application.mapper

import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent
import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreatedEvent

fun MessageAddedEvent.toProto(): MessageCreatedEvent {
    val event = this@toProto
    return MessageCreatedEvent.newBuilder().apply {
        this.chatId = event.chatId
        this.message = event.message.toProto()
    }.build()
}

fun Message.toDto(chatId: String): MessageDto {
    val message = this@toDto
    return MessageDto.newBuilder().apply {
        this.chatId = chatId
        this.message = message.toProto()
    }.build()
}

fun Message.toProto(): ProtoMessage {
    val message = this@toProto
    return ProtoMessage.newBuilder().apply {
        this.userId = message.userId
        this.content = message.content
        this.sentTime =
            message.sentAt?.time?.let { time -> com.google.protobuf.Timestamp.newBuilder().setSeconds(time).build() }
    }.build()
}

fun Message.createEvent(chatId: String): MessageAddedEvent {
    val message = this@createEvent
    return MessageAddedEvent(chatId, message)
}
