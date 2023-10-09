package rys.nats.natsservice

import rys.rest.model.MongoChat

interface ProtobufService {
    fun deserializeChat(serialisedChat: ByteArray?): MongoChat
    
    fun serializeChat(chat: MongoChat?): ByteArray 
    
    fun deserializeChatList(serialisedChats: ByteArray?): List<MongoChat>

    fun serializeMongoChats(chats: List<MongoChat>?): ByteArray

    fun serializeDeleteRequest(chatId: String?): ByteArray

    fun deserializeDeleteRequest(serializedRequest: ByteArray?): String

    fun serializeDeleteResponse(result: Boolean?): ByteArray

    fun deserializeDeleteResponse(serialisedDeleteResponse: ByteArray?): Boolean

    fun serializeFindChatRequest(chatId: String?): ByteArray

    fun deserializeFindChatRequest(serializedChatFindRequest: ByteArray?): String

    fun serializeUpdateRequest(id: String?, chat: MongoChat?): ByteArray

    fun deserializeUpdateRequest(serializedRequest: ByteArray?): Pair<String,MongoChat>
}
