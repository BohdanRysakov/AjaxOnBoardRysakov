package rys.nats.utils

import org.bson.types.ObjectId
import rys.nats.protostest.Mongochat
import rys.nats.protostest.Mongochat.ChatDeleteResponse
import rys.rest.model.MongoChat


object NatsMongoChatParser {

    fun deserializeChat(serialisedChat: ByteArray): MongoChat {
        val createdChat = Mongochat.Chat.parser()
            .parseFrom(serialisedChat)

        return MongoChat(
            id = if (createdChat.id != "null") ObjectId(createdChat.id) else null,
            name = createdChat.name,
            users = createdChat.usersList.map { ObjectId(it) })
    }

    fun serializeChat(chat: MongoChat?): ByteArray {
        if (chat == null) {
            return Mongochat.Chat
                .newBuilder()
                .setId("null")
                .setName("null")
                .build().toByteArray()
        }

        return Mongochat.Chat
            .newBuilder()
            .setId(chat.id.toString())
            .setName(chat.name)
            .addAllUsers(chat.users.map { it.toString() }).build().toByteArray()
    }

    fun deserializeChatList(serialisedChats: ByteArray): List<MongoChat> =
        Mongochat.ChatList
            .parser()
            .parseFrom(serialisedChats)
            .chatsList
            .map {
                MongoChat(
                    id = if (it.id != "null") ObjectId(it.id) else null,
                    name = it.name,
                    users = it.usersList.map { ObjectId(it) })
            }

    fun serializeMongoChats(chat: List<MongoChat>): ByteArray =
        Mongochat.ChatList.newBuilder()
            .addAllChats(chat.map {
                Mongochat.Chat.newBuilder()
                    .setId(it.id.toString())
                    .setName(it.name)
                    .addAllUsers(it.users.map { it.toString() })
                    .build()
            })
            .build().toByteArray()

    fun serializeDeleteRequest(chatId: String): ByteArray {
        return Mongochat.ChatDeleteRequest
            .newBuilder()
            .setRequestId(chatId)
            .build()
            .toByteArray()
    }

    fun deserializeDeleteRequest(serializedRequest: ByteArray): String {
        return Mongochat.ChatDeleteRequest
            .parser()
            .parseFrom(serializedRequest)
            .requestId
    }

    fun serializeDeleteResponse(result: Boolean): ByteArray {
        return ChatDeleteResponse
            .newBuilder()
            .setResult(result)
            .build()
            .toByteArray()
    }

    fun deserializeDeleteResponse(serialisedDeleteResponse: ByteArray): Boolean =
        ChatDeleteResponse.parser()
            .parseFrom(serialisedDeleteResponse).result

    fun serializeFindChatRequest(chatId: String): ByteArray {
        return Mongochat.ChatFindRequest
            .newBuilder()
            .setId(chatId)
            .build()
            .toByteArray()
    }

    fun deserializeFindChatRequest(serializedChatFindRequest: ByteArray): String =
        Mongochat.ChatFindRequest
            .parser()
            .parseFrom(serializedChatFindRequest)
            .id

    fun serializeUpdateRequest(id: String, chat: MongoChat): ByteArray {
        return Mongochat.ChatUpdateRequest
            .newBuilder()
            .setRequestId(id)
            .setChat(
                Mongochat.Chat.newBuilder()
                    .setId(chat.id.toString())
                    .setName(chat.name)
                    .addAllUsers(chat.users.map { it.toString() })
                    .build()
            )
            .build()
            .toByteArray()
    }

    fun deserializeUpdateRequest(serializedRequest: ByteArray): Pair<String,MongoChat> {
        val request = Mongochat.ChatUpdateRequest
            .parser()
            .parseFrom(serializedRequest)

        return Pair(
            request.requestId,
            MongoChat(
                id = if (request.chat.id != "null") ObjectId(request.chat.id) else null,
                name = request.chat.name,
                users = request.chat.usersList.map { ObjectId(it) })
        )
    }

}
