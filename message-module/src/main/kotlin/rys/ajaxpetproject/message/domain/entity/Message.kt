package rys.ajaxpetproject.message.domain.entity

import com.google.protobuf.Timestamp
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage
import rys.ajaxpetproject.request.message.create.proto.CreateEvent
import java.time.Instant
import java.util.*

data class Message (
    val id: String? = null,
    val userId: String,
    val content: String,
    val sentAt: Date
)


fun ProtoMessage.toModel(): Message {
    return Message(
        userId = this.userId,
        content = this.content,
        sentAt = Date(this.sentTime.seconds)
    )
}

fun Message.toProto(): ProtoMessage {
    val message = this@toProto
    return ProtoMessage.newBuilder().apply {
        this.userId = message.userId
        this.content = message.content
        this.sentTime = Timestamp.newBuilder().setSeconds(message.sentAt.time).build()
    }.build()
}

//fun Message.createEvent(chatId: String): CreateEvent.MessageCreatedEvent {
//    val message = this@createEvent
//    return MessageCreatedEvent.newBuilder().apply {
//        this.chatId = chatId
//        this.message = message.toProto()
//    }.build()
//}
