package rys.ajaxpetproject.chat.infrastructure.kafka.mapper

import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent
import rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent.mapper.toProto
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage
import rys.ajaxpetproject.request.message.create.proto.CreateEvent

fun MessageAddedEvent.toProto(): CreateEvent.MessageCreatedEvent {
    val event = this@toProto
    return CreateEvent.MessageCreatedEvent.newBuilder().apply {
        this.chatId = event.chatId
        this.message = event.message.toProto()
    }.build()
}

internal fun Message.toProto(): ProtoMessage {
    val message = this@toProto
    return ProtoMessage.newBuilder().apply {
        this.userId = message.userId
        this.content = message.content
        this.sentTime =
            message.sentAt?.time?.let { time -> com.google.protobuf.Timestamp.newBuilder().setSeconds(time).build() }
    }.build()
}
