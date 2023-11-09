package rys.ajaxpetproject.chat.domain.entity

import rys.ajaxpetproject.commonmodels.message.proto.MessageDto
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage
import rys.ajaxpetproject.commonmodels.chat.proto.Chat as ProtoChat
import java.util.*

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
    val sentAt: Date? = null
)

fun Chat.toProto(): ProtoChat {
    val chat = this@toProto
    return ProtoChat.newBuilder().apply {
        this.id = chat.id
        this.name = chat.name
        this.usersList.addAll(chat.users)
        this.messagesList.addAll(chat.messages)
    }.build()
}

fun ProtoChat.toDomainModel(): Chat {
    return Chat(
        id = this.id,
        name = this.name,
        users = this.usersList,
        messages = this.messagesList
    )
}

fun Chat.toMongoChat(): MongoChat {
    return MongoChat(
        id = this.id,
        name = this.name,
        users = this.users,
        messages = this.messages
    )
}

fun MongoChat.toDomainChat(): Chat {
    return Chat(
        id = this.id,
        name = this.name,
        users = this.users,
        messages = this.messages
    )
}

fun MongoMessage.toDomainMessage(): Message {
    return Message(
        id = this.id,
        userId = this.userId,
        content = this.content,
        sentAt = this.sentAt
    )
}

fun Message.toMongoMessage(): MongoMessage {
    return MongoMessage(
        id = this.id,
        userId = this.userId,
        content = this.content,
    )
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

fun ProtoMessage.toModel(): Message {
    return Message(
        userId = this.userId,
        content = this.content,
        sentAt = this.sentTime?.let { Date(it.seconds) }
    )
}
