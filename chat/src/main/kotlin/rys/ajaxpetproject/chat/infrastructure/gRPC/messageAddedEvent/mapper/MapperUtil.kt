package rys.ajaxpetproject.chat.infrastructure.gRPC.messageAddedEvent.mapper

import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.commonmodels.chat.proto.Chat as ProtoChat
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage

internal fun Chat.toProto(): ProtoChat {
    val chat = this@toProto
    return ProtoChat.newBuilder().apply {
        this.id = chat.id
        this.name = chat.name
        addAllUsers(chat.users)
        addAllMessages(chat.messages)
    }.build()
}

internal fun ProtoChat.toDomainModel(): Chat {
    return Chat(
        id = this.id,
        name = this.name,
        users = this.usersList,
        messages = this.messagesList
    )
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
