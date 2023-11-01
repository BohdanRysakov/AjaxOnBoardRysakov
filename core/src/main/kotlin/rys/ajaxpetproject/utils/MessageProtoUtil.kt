package rys.ajaxpetproject.utils

import com.google.protobuf.Timestamp
import rys.ajaxpetproject.commonmodels.message.proto.Message
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.request.message.create.proto.CreateEvent.MessageCreateEvent
import java.util.Date

fun Message.toModel(): MongoMessage {
    return MongoMessage(
        userId = this.userId,
        content = this.content,
        sentAt = Date(this.sentTime.seconds)
    )
}

fun MongoMessage.toProto(): Message {
    val message = this@toProto
    return Message.newBuilder().apply {
        this.userId = message.userId
        this.content = message.content
        this.sentTime = Timestamp.newBuilder().setSeconds(message.sentAt.time).build()
    }.build()
}

fun MongoMessage.createEvent(chatId: String): MessageCreateEvent {
    val message = this@createEvent
    return MessageCreateEvent.newBuilder().apply {
        this.chatId = chatId
        this.message = message.toProto()
    }.build()
}
