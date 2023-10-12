package rys.ajaxpetproject.nats.controller

import io.nats.client.Connection
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteRequest
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteResponse
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllResponse
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneRequest
import rys.ajaxpetproject.request.findOne.create.proto.ChatFindOneResponse
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateRequest
import rys.ajaxpetproject.request.update.create.proto.ChatUpdateResponse
import rys.ajaxpetproject.subjects.ChatSubjectsV1

@RestController
@RequestMapping("/nats/chats")
@Suppress("ReturnCount", "WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class RestChatController(private val natsConnection: Connection) {

    @PostMapping("")
    fun createChat(@Valid @RequestBody mongoChat: MongoChat): ResponseEntity<Any>? {
        val request = ChatCreateRequest.newBuilder().apply {
            this.chat = chatBuilder.apply {
                this.id = mongoChat.id.toString()
                this.name = mongoChat.name
                mongoChat.users.forEach {
                    this.addUsers(it.toString())
                }
            }.build()
        }.build()

        val response = ChatCreateResponse.parseFrom(
            natsConnection.request(
                ChatSubjectsV1.ChatRequest.CREATE, request.toByteArray()
            ).get().data
        )

        when (response.responseCase) {
            ChatCreateResponse.ResponseCase.SUCCESS -> {
                val success = response.success.result
                val newChat = MongoChat(
                    id = ObjectId(success.id),
                    name = success.name,
                    users = success.usersList.map { ObjectId(it) })
                return ResponseEntity(newChat, HttpStatus.CREATED)
            }
            ChatCreateResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(response.failure.message.toString(), HttpStatus.BAD_REQUEST)
            }
            ChatCreateResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity("Unexpected internal error. See logs for details", HttpStatus.BAD_REQUEST)
            }
        }
    }

    @GetMapping("")
    fun findAllChats(): ResponseEntity<Any> {
        val response = ChatFindAllResponse.parseFrom(
            natsConnection.request(
                ChatSubjectsV1.ChatRequest.FIND_ALL,
                ByteArray(0)
            ).get().data
        )

        when (response.responseCase) {
            ChatFindAllResponse.ResponseCase.SUCCESS -> {
                val chats = response.success.resultList.map {
                    MongoChat(
                        id = ObjectId(it.id),
                        name = it.name,
                        users = it.usersList.map(::ObjectId)
                    )
                }
                return ResponseEntity(chats, HttpStatus.OK)
            }
            ChatFindAllResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(response.failure.message.toString(), HttpStatus.BAD_REQUEST)
            }
            ChatFindAllResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity("Unexpected internal error. See logs for details", HttpStatus.BAD_REQUEST)
            }
        }
    }

    @GetMapping("/{id}")
    fun findChat(@PathVariable id: String): ResponseEntity<Any> {
        val request = ChatFindOneRequest.newBuilder().apply {
            this.id = id
        }.build()

        val response = ChatFindOneResponse.parseFrom(
            natsConnection.request(
                ChatSubjectsV1.ChatRequest.FIND_ONE, request.toByteArray()
            ).get().data
        )

        when (response.responseCase) {
            ChatFindOneResponse.ResponseCase.SUCCESS -> {
                val success = response.success.result
                val chat = MongoChat(
                    id = ObjectId(success.id),
                    name = success.name,
                    users = success.usersList.map { ObjectId(it) }
                )
                return ResponseEntity(chat, HttpStatus.OK)
            }
            ChatFindOneResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(),
                    HttpStatus.BAD_REQUEST
                )
            }
            ChatFindOneResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error. See logs for details", HttpStatus.BAD_REQUEST
                )
            }
        }
    }

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: String): ResponseEntity<Any> {
        val request = ChatDeleteRequest.newBuilder().apply {
            this.requestId = id
        }.build()

        val response = ChatDeleteResponse.parseFrom(
            natsConnection.request(
                ChatSubjectsV1.ChatRequest.DELETE,request.toByteArray()
            ).get().data
        )

        when (response.responseCase) {
            ChatDeleteResponse.ResponseCase.SUCCESS -> {
                return ResponseEntity(
                    response.success.result, HttpStatus.OK
                )
            }
            ChatDeleteResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(), HttpStatus.BAD_REQUEST
                )
            }
            ChatDeleteResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error. See logs for details", HttpStatus.BAD_REQUEST
                )
            }
        }
    }

    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: String, @RequestBody chat: MongoChat): ResponseEntity<Any> {
        val request = ChatUpdateRequest.newBuilder().apply {
            this.requestId = id
            this.chat = chatBuilder.apply {
                this.id = chat.id.toString()
                this.name = chat.name
                chat.users.forEach {
                    this.addUsers(it.toString())
                }
            }.build()
        }.build()

        val response = ChatUpdateResponse.parseFrom(
            natsConnection.request(
                ChatSubjectsV1.ChatRequest.UPDATE, request.toByteArray()
            ).get().data
        )

        when (response.responseCase) {
            ChatUpdateResponse.ResponseCase.SUCCESS -> {
                val success = response.success.result
                val updatedChat = MongoChat(
                    id = ObjectId(success.id),
                    name = success.name,
                    users = success.usersList.map { ObjectId(it) }
                )
                return ResponseEntity(updatedChat, HttpStatus.OK)
            }
            ChatUpdateResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(), HttpStatus.BAD_REQUEST
                )
            }
            ChatUpdateResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error. See logs for details", HttpStatus.BAD_REQUEST
                )
            }
        }
    }
}
