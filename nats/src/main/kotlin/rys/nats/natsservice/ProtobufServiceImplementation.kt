package rys.nats.natsservice

import org.springframework.stereotype.Service
import rys.nats.exception.InternalException
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat

@Service
@Suppress("TooManyFunctions")
class ProtobufServiceImplementation : ProtobufService {

    override fun deserializeChat(serialisedChat: ByteArray?): MongoChat {
        if (serialisedChat == null) throw InternalException()
        return NatsValidMongoChatParser.deserializeChat(serialisedChat)
    }

    override fun serializeChat(chat: MongoChat?): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deserializeChatList(serialisedChats: ByteArray?): List<MongoChat> {
        if (serialisedChats == null) throw InternalException()
        return NatsValidMongoChatParser.deserializeChatList(serialisedChats)
    }

    override fun deserializeDeleteRequest(serializedRequest: ByteArray?): String {
        if (serializedRequest == null) throw InternalException()
        return NatsValidMongoChatParser.deserializeDeleteRequest(serializedRequest)
    }

    override fun deserializeDeleteResponse(serialisedDeleteResponse: ByteArray?): Boolean {
        if (serialisedDeleteResponse == null) throw InternalException()
        return NatsValidMongoChatParser.deserializeDeleteChatResponse(serialisedDeleteResponse)
    }

    override fun deserializeFindChatRequest(serializedChatFindRequest: ByteArray?): String {
        if (serializedChatFindRequest == null) throw InternalException()
        return NatsValidMongoChatParser.deserializeFindChatRequest(serializedChatFindRequest)
    }

    override fun deserializeUpdateRequest(serializedRequest: ByteArray?): Pair<String, MongoChat> {
        if (serializedRequest == null) throw InternalException()
        return NatsValidMongoChatParser.deserializeUpdateRequest(serializedRequest)
    }

    override fun serializeUpdateRequest(id: String?, chat: MongoChat?): ByteArray {
        if (id == null || chat == null) throw InternalException()
        return NatsValidMongoChatParser.serializeUpdateRequest(id, chat)
    }

    override fun serializeFindChatRequest(chatId: String?): ByteArray {
        if (chatId == null) throw InternalException()
        return NatsValidMongoChatParser.serializeFindChatRequest(chatId)
    }

    override fun serializeDeleteResponse(result: Boolean?): ByteArray {
        if (result == null) throw InternalException()
        return NatsValidMongoChatParser.serializeDeleteChatResponse(result)
    }

    override fun serializeMongoChats(chats: List<MongoChat>?): ByteArray {
        if (chats == null) throw InternalException()
        return NatsValidMongoChatParser.serializeMongoChatList(chats)
    }

    override fun serializeDeleteRequest(chatId: String?): ByteArray {
        if (chatId == null) throw InternalException()
        return NatsValidMongoChatParser.serializeDeleteChatRequest(chatId)
    }

//    override fun serializeChat(chat: MongoChat?): ByteArray {
//        if (chat == null) throw SerialiseException()
//        return NatsMongoChatParser.serializeChat(chat)

}
