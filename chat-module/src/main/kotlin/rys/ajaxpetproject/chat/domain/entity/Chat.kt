package rys.ajaxpetproject.chat.domain.entity

import com.google.protobuf.Timestamp
import java.util.*
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage

data class Chat(
    val id: String? = null,
    val name: String?,
    val users: List<String> = emptyList(),
    val messages: List<String> = emptyList()
)

class Message(
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
