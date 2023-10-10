package rys.nats.utils

import rys.nats.protostest.Mongochat
import rys.nats.protostest.Mongochat.ChatCreateRequest
import rys.nats.protostest.Mongochat.ChatCreateResponse
import rys.nats.protostest.Mongochat.ChatDeleteRequest
import rys.nats.protostest.Mongochat.ChatDeleteResponse
import rys.nats.protostest.Mongochat.ChatFindAllResponse
import rys.nats.protostest.Mongochat.ChatFindOneRequest
import rys.nats.protostest.Mongochat.ChatFindOneResponse
import rys.nats.protostest.Mongochat.ChatUpdateRequest
import rys.nats.protostest.Mongochat.ChatUpdateResponse

@Suppress("TooManyFunctions", "NestedBlockDepth")
object NatsValidMongoChatParser {

    fun serializeDeleteChatRequest(request: ChatDeleteRequest): ByteArray {
        return request.toByteArray()
    }

    fun deserializeDeleteRequest(request: ByteArray): ChatDeleteRequest {
        return ChatDeleteRequest.parser().parseFrom(request)
    }

    fun serializeDeleteChatResponse(response: ChatDeleteResponse): ByteArray {
        return response.toByteArray()
    }

    fun deserializeDeleteChatResponse(response: ByteArray): ChatDeleteResponse {
        return ChatDeleteResponse.parseFrom(response)
    }

    fun serializeFindChatRequest(request: ChatFindOneRequest): ByteArray {
        return request.toByteArray()
    }

    fun deserializeFindChatRequest(request: ByteArray): ChatFindOneRequest {
        return ChatFindOneRequest.parser().parseFrom(request)
    }

    fun serializeFindChatResponse(response: ChatFindOneResponse): ByteArray {
        return response.toByteArray()
    }

    fun deserializeFindChatResponse(response: ByteArray): ChatFindOneResponse {
        return ChatFindOneResponse.parseFrom(response)
    }

    fun serializeUpdateRequest(request: ChatUpdateRequest): ByteArray {
        return request.toByteArray()
    }

    fun deserializeUpdateRequest(serializedRequest: ByteArray): ChatUpdateRequest {
        return ChatUpdateRequest
            .parser()
            .parseFrom(serializedRequest)
    }

    fun serializeUpdateResponse(response: ChatUpdateResponse): ByteArray {
        return response.toByteArray()
    }

    fun deserializeUpdateResponse(response: ByteArray): ChatUpdateResponse {
        return ChatUpdateResponse
            .parseFrom(response)

    }

    fun serializeCreateChatRequest(request: ChatCreateRequest): ByteArray {
        return request.toByteArray()
    }

    fun deserializeCreateChatRequest(request: ByteArray): ChatCreateRequest {
        return ChatCreateRequest.parseFrom(request)
    }

    fun serializeCreateChatResponse(response: ChatCreateResponse): ByteArray {
        return response.toByteArray()
    }

    fun deserializeCreateChatResponse(response: ByteArray): ChatCreateResponse {
        return ChatCreateResponse.parseFrom(response)
    }

    fun serializeFindChatsRequest(): ByteArray {
        return ChatFindOneRequest.newBuilder().build().toByteArray()
    }

    fun deserializeFindChatsRequest(request: ByteArray): ChatFindOneRequest {
        return ChatFindOneRequest.parseFrom(request)
    }

    fun serializeFindChatsResponse(response: ChatFindAllResponse): ByteArray {
        return response.toByteArray()
    }

    fun deserializeFindChatsResponse(response: ByteArray): ChatFindAllResponse {
        return ChatFindAllResponse.parseFrom(response)
    }

}
