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
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateRequest
import rys.ajaxpetproject.request.chat.create.proto.ChatCreateResponse
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteRequest
import rys.ajaxpetproject.request.chat.delete.proto.ChatDeleteResponse
import rys.ajaxpetproject.request.findAll.create.proto.ChatFindAllRequest
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
    fun createChat(@RequestBody mongoChat: MongoChat): Mono<MongoChat> {

        val request = ChatCreateRequest.newBuilder().apply {
            this.chat = chatBuilder.apply {
                this.id = mongoChat.id.toString()
                this.name = mongoChat.name
                mongoChat.users.forEach {
                    this.addUsers(it.toString())
                }
            }.build()
        }.build()

        val response: Mono<ResponseEntity<ChatCreateResponse>> = Mono.fromCallable {
            ChatCreateResponse.parseFrom(
                natsConnection.request(
                    ChatSubjectsV1.ChatRequest.CREATE, request.toByteArray()
                ).get().data
            )
        }
            .map { response ->
                ResponseEntity.ok().body(response)
            }
            .subscribeOn(Schedulers.boundedElastic())
        return response
            .flatMap { chatCreateResponse ->
                val chat = chatCreateResponse.body!!.success.result
                val mongoChat2 = MongoChat(
                    id = ObjectId(chat.id),
                    name = chat.name!!,
                    users = chat.usersList.map { ObjectId(it) },
                    messages = emptyList()
                )
                Mono.just(mongoChat2)
            }
    }

    @GetMapping("/")
    fun findAllChats(): Mono<List<MongoChat>> {
        val request = ChatFindAllRequest.newBuilder().build()
        return Mono.fromCallable {
            ChatFindAllResponse.parseFrom(
                natsConnection.request(
                    ChatSubjectsV1.ChatRequest.FIND_ALL, request.toByteArray()
                ).get().data
            )
        }
            .map { response ->

                response!!.success.resultList.map { chat ->
                    MongoChat(
                        id = ObjectId(chat.id),
                        name = chat.name!!,
                        users = chat.usersList.map { ObjectId(it) },
                        messages = chat.messagesList.map { ObjectId(it) }
                    )
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
    }

    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: String): Mono<ResponseEntity<ChatFindOneResponse>> {
        val request = ChatFindOneRequest.newBuilder().apply {
            this.id = id
        }.build()
        val response: Mono<ResponseEntity<ChatFindOneResponse>> = Mono.fromCallable {
            ChatFindOneResponse.parseFrom(
                natsConnection.request(
                    ChatSubjectsV1.ChatRequest.FIND_ONE, request.toByteArray()
                ).get().data
            )
        }
            .map { response ->
                // Transform the response into the desired format
                ResponseEntity.ok().body(response)
            }
            .subscribeOn(Schedulers.boundedElastic())
        return response
    }

}
