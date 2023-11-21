package rys.ajaxpetproject.chat.infrastructure.nats.mapper

import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import java.util.*
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

internal fun ProtoMessage.toDomainMessage(): Message {
    return Message(
        userId = this.userId,
        content = this.content,
        sentAt = this.sentTime?.let { Date(it.seconds) }
    )
}
