package rys.nats.controller

import io.nats.client.Connection
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.nats.natsservice.ProtobufService
import rys.rest.model.MongoChat

@RestController
@RequestMapping("/nats/chats")
class RestChatController(private val natsConnection: Connection,private val protoService: ProtobufService) {

    @PostMapping("")
    fun createChat(@RequestBody mongoChat: MongoChat): ResponseEntity<MongoChat> =
        ResponseEntity(
            protoService.deserializeChat(
                natsConnection.request(
                    "chat.create",
                    protoService.serializeChat(mongoChat)
                ).get().data
            ),
            HttpStatus.CREATED
        )

//    @GetMapping("")
//    fun findAllChats(): ResponseEntity<List<MongoChat>> =
//        ResponseEntity(
//            protoService.deserializeChatList(
//                natsConnection.request(
//                    "chat.findAll", "find all chats".toByteArray()
//                ).get().data
//            ),
//            HttpStatus.OK
//        )
//
//    @GetMapping("/{id}")
//    fun findChat(@PathVariable id: String): ResponseEntity<MongoChat> =
//        ResponseEntity(
//            deserializeFindChatResponse(
//                natsConnection.request(
//                    "chat.findOne", protoService.serializeFindChatRequest(id)
//                ).get().data
//            ),
//            HttpStatus.OK
//        )
//
//    @DeleteMapping("/{id}")
//    fun deleteChat(@PathVariable id: String): ResponseEntity<Boolean> =
//        ResponseEntity(
//            protoService.deserializeDeleteResponse(
//                natsConnection.request(
//                    "chat.delete", protoService.serializeDeleteRequest(id)
//                ).get().data
//            ),
//            HttpStatus.OK
//        )
//
//    @PutMapping("/{id}")
//    fun updateChat(@PathVariable id: String, @RequestBody chat: MongoChat): ResponseEntity<MongoChat> {
//         return ResponseEntity(
//            protoService.deserializeChat(
//                natsConnection.request(
//                    "chat.update", protoService.serializeUpdateRequest(id,chat)
//                ).get().data
//            ),
//            HttpStatus.CREATED
//        )
//
//    }


}




