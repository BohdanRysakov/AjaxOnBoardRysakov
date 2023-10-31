package rys.ajaxpetproject.utils

import rys.ajaxpetproject.commonmodels.chat.proto.Chat
import rys.ajaxpetproject.model.MongoChat

fun Chat.toModel() : MongoChat {
    return MongoChat(
        id = this.id,
        name = this.name,
        users = this.usersList,
        messages = this.messagesList
    )
}

fun MongoChat.toProto() : Chat {
    val chat = this@toProto
    return Chat.newBuilder().apply {
        this.id = chat.id.toString()
        this.name = chat.name
        addAllUsers(chat.users)
        addAllMessages(chat.messages)
    }.build()
}
