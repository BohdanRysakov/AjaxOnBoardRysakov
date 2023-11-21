package rys.ajaxpetproject.chat.infrastructure.mongo.mapper

import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage

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
