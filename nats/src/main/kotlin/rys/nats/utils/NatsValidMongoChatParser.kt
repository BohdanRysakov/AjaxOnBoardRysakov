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

@Suppress("TooManyFunctions","NestedBlockDepth")
object NatsValidMongoChatParser {

    fun serializeDeleteChatRequest(request: ChatDeleteRequest): ByteArray {
        return ChatDeleteRequest
            .newBuilder()
            .setRequestId(request.requestId)
            .build()
            .toByteArray()
    }

    fun deserializeDeleteRequest(request: ByteArray): ChatDeleteRequest {
        return ChatDeleteRequest
            .parser()
            .parseFrom(request)
    }

    fun serializeDeleteChatResponse(response: ChatDeleteResponse): ByteArray {
        // ASK: Can Failure Deletion be success operation?
        return ChatDeleteResponse.newBuilder().apply {
            if (this.hasSuccess()) {
                successBuilder.result = response.success.result
            } else {
                failureBuilder.message = "Cannot delete chat. See internal logs for more details."
                failureBuilder.apply {
                    if (response.failure.hasInternalError()) {
                        this.internalErrorBuilder
                    } else {
                        this.notFoundBuilder
                    }
                }
            }
        }.build().toByteArray()
    }

    fun deserializeDeleteChatResponse(response: ByteArray): ChatDeleteResponse {
        return ChatDeleteResponse
            .parseFrom(response)
    }


    fun serializeFindChatRequest(request: ChatFindOneRequest): ByteArray {
        return ChatFindOneRequest
            .newBuilder()
            .setId(request.id)
            .build()
            .toByteArray()
    }

    fun deserializeFindChatRequest(request: ByteArray): ChatFindOneRequest =
        ChatFindOneRequest
            .parser()
            .parseFrom(request)

    fun serializeFindChatResponse(response: ChatFindOneResponse): ByteArray {
        return ChatFindOneResponse.newBuilder().apply {
            if (response.hasSuccess()) {
                successBuilder.result = response.success.result
            } else {
                failureBuilder.message = "Cannot find chat. See internal logs for more details."
                failureBuilder.apply {
                    if (response.failure.hasInternalError()) {
                        this.internalErrorBuilder
                    } else {
                        this.notFoundBuilder
                    }
                }
            }
        }.build().toByteArray()
    }

    fun deserializeFindChatResponse(response: ByteArray): ChatFindOneResponse {
        return ChatFindOneResponse.parseFrom(response)
    }


    fun serializeUpdateRequest(request: ChatUpdateRequest): ByteArray {
        return ChatUpdateRequest
            .newBuilder()
            .setRequestId(request.requestId)
            .setChat(
                Mongochat.Chat.newBuilder()
                    .setId(request.chat.id)
                    .setName(request.chat.name)
                    .addAllUsers(request.chat.usersList)
                    .build()
            )
            .build()
            .toByteArray()
    }

    fun deserializeUpdateRequest(serializedRequest: ByteArray): ChatUpdateRequest {
        return ChatUpdateRequest
            .parser()
            .parseFrom(serializedRequest)
    }

    fun serializeUpdateResponse(response: ChatUpdateResponse): ByteArray {
        return ChatUpdateResponse.newBuilder().apply {
            if (response.hasSuccess()) {
                successBuilder.result = response.success.result
            } else {
                failureBuilder.message = "Cannot update chat. See internal logs for more details."
                failureBuilder.apply {
                    if (response.failure.hasInternalError()) {
                        this.internalErrorBuilder
                    } else {
                        this.notFoundBuilder
                    }
                }
            }
        }.build().toByteArray()
    }

    fun deserializeUpdateResponse(response: ByteArray): ChatUpdateResponse {
        return ChatUpdateResponse
            .parseFrom(response)

    }

    fun serializeCreateChatRequest(request : ChatCreateRequest) : ByteArray {
        return ChatCreateRequest.newBuilder().apply {
            chatBuilder.id = request.chat.id
            chatBuilder.name = request.chat.name
            request.chat.usersList.forEach {
                chatBuilder.addUsers(it)
            }
        }.build().toByteArray()
    }

    fun deserializeCreateChatRequest(request: ByteArray): ChatCreateRequest {
        return ChatCreateRequest.parseFrom(request)
    }

    fun serializeCreateChatResponse(response:ChatCreateResponse): ByteArray {
        return ChatCreateResponse.newBuilder().apply {
            if (response.hasSuccess()) {
                successBuilder.result = response.success.result
            } else {
                failureBuilder.message = "Cannot create chat. See internal logs for more details."
                failureBuilder.apply {
                    if (response.failure.hasInternalError()) {
                        this.internalErrorBuilder
                    } else {
                        this.notFoundBuilder
                    }
                }
            }
        }.build().toByteArray()
    }

    fun deserializeCreateChatResponse(response: ByteArray): ChatCreateResponse {
        return ChatCreateResponse.parseFrom(response)
    }


    fun serializeFindChatsRequest(): ByteArray {
        return ChatFindOneRequest.newBuilder().build().toByteArray()
    }

    fun deserializeFindChatsRequest(request: ByteArray): ChatFindOneRequest{
        return ChatFindOneRequest.parseFrom(request)
    }

    fun serializeFindChatsResponse(response: ChatFindAllResponse): ByteArray {
        return ChatFindAllResponse.newBuilder().apply {
            if (response.hasSuccess()) {
                response.success.resultList.forEach {
                    successBuilder.addResult(it)
                }
            } else {
                failureBuilder.message = "Cannot find chats. See internal logs for more details."
                failureBuilder.apply {
                    if (response.failure.hasInternalError()) {
                        this.internalErrorBuilder
                    } else {
                        this.notFoundBuilder
                    }
                }
            }
        }.build().toByteArray()
    }

    fun deserializeFindChatsResponse(response: ByteArray): ChatFindAllResponse {
        return ChatFindAllResponse.parseFrom(response)
    }

}
