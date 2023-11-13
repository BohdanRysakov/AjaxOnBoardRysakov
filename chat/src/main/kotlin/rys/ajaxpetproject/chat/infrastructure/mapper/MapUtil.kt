package rys.ajaxpetproject.chat.infrastructure.mapper

import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.chat.domain.event.MessageAddedEvent
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.request.message.create.proto.CreateEvent
import rys.ajaxpetproject.commonmodels.message.proto.Message as ProtoMessage
import rys.ajaxpetproject.commonmodels.chat.proto.Chat as ProtoChat

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

internal fun Chat.toMongoChat(): MongoChat {
    return MongoChat(
        id = this.id,
        name = this.name,
        users = this.users,
        messages = this.messages
    )
}

internal fun MongoChat.toDomainChat(): Chat {
    return Chat(
        id = this.id,
        name = this.name,
        users = this.users,
        messages = this.messages
    )
}

internal fun MongoMessage.toDomainMessage(): Message {
    return Message(
        id = this.id,
        userId = this.userId,
        content = this.content,
        sentAt = this.sentAt
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

internal fun MessageAddedEvent.toProto(): CreateEvent.MessageCreatedEvent {
    val messageEvent = this
    return CreateEvent.MessageCreatedEvent.newBuilder().apply {
        this.chatId = messageEvent.chatId
        this.message = messageEvent.message.toProto()
    }.build()
}
