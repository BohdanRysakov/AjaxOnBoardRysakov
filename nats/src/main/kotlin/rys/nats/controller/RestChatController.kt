package rys.nats.controller

import io.nats.client.Connection
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.nats.exception.InternalException
import rys.nats.protostest.Mongochat
import rys.nats.utils.NatsValidMongoChatParser
import rys.rest.model.MongoChat

@RestController
@RequestMapping("/nats/chats")
class RestChatController(private val natsConnection: Connection) {

    @PostMapping("")
    fun createChat(@Valid @RequestBody mongoChat: MongoChat): ResponseEntity<Any>? {
        val request = Mongochat.ChatCreateRequest.newBuilder()
            .apply {
                this.chat = Mongochat.Chat.newBuilder()
                    .apply {
                        this.id = mongoChat.id.toString()
                        this.name = mongoChat.name
                        mongoChat.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()

        val response = NatsValidMongoChatParser.deserializeCreateChatResponse(
            natsConnection.request(
                "chat.create",
                NatsValidMongoChatParser.serializeCreateChatRequest(request)
            )
                .get()
                .data
        )

        when (response.responseCase) {
            Mongochat.ChatCreateResponse.ResponseCase.SUCCESS -> {
                val success = response.success.result
                val newChat = MongoChat(
                    id = ObjectId(success.id),
                    name = success.name,
                    users = success.usersList.map { ObjectId(it) })
                return ResponseEntity(
                    newChat,
                    HttpStatus.CREATED
                )
            }

            Mongochat.ChatCreateResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(),
                    HttpStatus.BAD_REQUEST
                )
            }

            Mongochat.ChatCreateResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error",
                    HttpStatus.BAD_REQUEST
                )
            }

        }

    }

    @GetMapping("")
    fun findAllChats(): ResponseEntity<Any> {
        val response = NatsValidMongoChatParser.deserializeFindChatsResponse(
            natsConnection.request(
                "chat.findAll",
                NatsValidMongoChatParser.serializeFindChatsRequest()
            ).get().data
        )

        when (response.responseCase) {
            Mongochat.ChatFindAllResponse.ResponseCase.SUCCESS -> {

                val chats = response.success.resultList.map {
                    MongoChat(
                        id = ObjectId(it.id),
                        name = it.name,
                        users = it.usersList.map { ObjectId(it) }
                    )
                }

                return ResponseEntity(chats, HttpStatus.OK)
            }

            Mongochat.ChatFindAllResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(response.failure.message.toString(), HttpStatus.BAD_REQUEST)
            }

            Mongochat.ChatFindAllResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity("Unexpected internal error. See logs for details", HttpStatus.BAD_REQUEST)
            }
        }
    }


    @GetMapping("/{id}")
    fun findChat(@PathVariable id: String): ResponseEntity<Any> {

        val request = Mongochat.ChatFindOneRequest.newBuilder()
            .apply {
                this.id = id
            }.build()

        val response = NatsValidMongoChatParser.deserializeFindChatResponse(
            natsConnection.request(
                "chat.findOne",
                NatsValidMongoChatParser.serializeFindChatRequest(request)
            ).get().data
        )

        when (response.responseCase) {
            Mongochat.ChatFindOneResponse.ResponseCase.SUCCESS -> {
                val success = response.success.result
                val chat = MongoChat(
                    id = ObjectId(success.id),
                    name = success.name,
                    users = success.usersList.map { ObjectId(it) }
                )
                return ResponseEntity(
                    chat,
                    HttpStatus.OK
                )
            }

            Mongochat.ChatFindOneResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(),
                    HttpStatus.BAD_REQUEST
                )
            }

            Mongochat.ChatFindOneResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error",
                    HttpStatus.BAD_REQUEST
                )
            }

        }

    }

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: String): ResponseEntity<Any> {
        val request = Mongochat.ChatDeleteRequest.newBuilder()
            .apply {
                this.requestId = id
            }.build()

        val response = NatsValidMongoChatParser.deserializeDeleteChatResponse(
            natsConnection.request(
                "chat.delete",
                NatsValidMongoChatParser.serializeDeleteChatRequest(request)
            ).get().data
        )

        when (response.responseCase) {
            Mongochat.ChatDeleteResponse.ResponseCase.SUCCESS -> {
                return ResponseEntity(
                    response.success.result,
                    HttpStatus.OK
                )
            }

            Mongochat.ChatDeleteResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(),
                    HttpStatus.BAD_REQUEST
                )
            }

            Mongochat.ChatDeleteResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error",
                    HttpStatus.BAD_REQUEST
                )
            }

        }
    }

    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: String, @RequestBody chat: MongoChat): ResponseEntity<Any> {

        val request = Mongochat.ChatUpdateRequest.newBuilder()
            .apply {
                this.requestId = id
                this.chat = Mongochat.Chat.newBuilder()
                    .apply {
                        this.id = chat.id.toString()
                        this.name = chat.name
                        chat.users.forEach {
                            this.addUsers(it.toString())
                        }
                    }.build()
            }.build()

        val response = NatsValidMongoChatParser.deserializeUpdateResponse(
            natsConnection.request(
                "chat.update",
                NatsValidMongoChatParser.serializeUpdateRequest(request)
            ).get().data
        )

        when (response.responseCase) {
            Mongochat.ChatUpdateResponse.ResponseCase.SUCCESS -> {

                val success = response.success.result

                val updatedChat = MongoChat(
                    id = ObjectId(success.id),
                    name = success.name,
                    users = success.usersList.map { ObjectId(it) }
                )
                return ResponseEntity(
                    updatedChat,
                    HttpStatus.OK
                )
            }

            Mongochat.ChatUpdateResponse.ResponseCase.FAILURE -> {
                return ResponseEntity(
                    response.failure.message.toString(),
                    HttpStatus.BAD_REQUEST
                )
            }

            Mongochat.ChatUpdateResponse.ResponseCase.RESPONSE_NOT_SET -> {
                return ResponseEntity(
                    "Unexpected internal error",
                    HttpStatus.BAD_REQUEST
                )
            }

        }
    }
}
