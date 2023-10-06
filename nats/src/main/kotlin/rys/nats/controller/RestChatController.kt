package rys.nats.controller

import io.nats.client.Connection
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.nats.utils.NatsMongoChatParser.deserializeChatList
import rys.nats.utils.NatsMongoChatParser.deserializeDeleteResponse
import rys.nats.utils.NatsMongoChatParser.serializeChat
import rys.nats.utils.NatsMongoChatParser.deserializeChat
import rys.nats.utils.NatsMongoChatParser.serializeDeleteRequest
import rys.nats.utils.NatsMongoChatParser.serializeFindChatRequest
import rys.nats.utils.NatsMongoChatParser.serializeUpdateRequest
import rys.rest.model.MongoChat

@RestController
@RequestMapping("/nats/chats")
class RestChatController(private val natsConnection: Connection) {

    @PostMapping("")
    fun createChat(@RequestBody mongoChat: MongoChat): ResponseEntity<MongoChat> =
        ResponseEntity(
            deserializeChat(
                natsConnection.request(
                    "chat.create",
                    serializeChat(mongoChat)
                ).get().data
            ),
            HttpStatus.CREATED
        )

    @GetMapping("")
    fun findAllChats(): ResponseEntity<List<MongoChat>> =
        ResponseEntity(
            deserializeChatList(
                natsConnection.request(
                    "chat.findAll", "find all chats".toByteArray()
                ).get().data
            ),
            HttpStatus.OK
        )

    @GetMapping("/{id}")
    fun findChat(@PathVariable id: String): ResponseEntity<MongoChat> =
        ResponseEntity(
            deserializeChat(
                natsConnection.request(
                    "chat.findOne", serializeFindChatRequest(id)
                ).get().data
            ),
            HttpStatus.OK
        )

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: String): ResponseEntity<Boolean> =
        ResponseEntity(
            deserializeDeleteResponse(
                natsConnection.request(
                    "chat.delete", serializeDeleteRequest(id)
                ).get().data
            ),
            HttpStatus.OK
        )

    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: String, @RequestBody chat: MongoChat): ResponseEntity<MongoChat> =
        ResponseEntity(
            deserializeChat(
                natsConnection.request(
                    "chat.update", serializeUpdateRequest(id,chat)
                ).get().data
            ),
            HttpStatus.CREATED
        )

}




