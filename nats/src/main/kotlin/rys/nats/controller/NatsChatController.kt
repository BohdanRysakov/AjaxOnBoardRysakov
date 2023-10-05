package rys.nats.controller

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.nats.natsservice.NatsChatService
import rys.nats.protostest.Mongochat
import rys.rest.model.MongoChat

@RestController
@RequestMapping("/nats/chats")
class NatsChatController( private val natsConnection: Connection) {

    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: MongoChat) : ResponseEntity<String> {

         val chatFromJson = Mongochat.ChatCreateRequest.newBuilder()
             .setId(mongoChat.id.toString())
             .setName(mongoChat.name)
             .addAllUsers(mongoChat.users.map {it.toString()})

        val response = natsConnection.request("chat.create", chatFromJson.build().toByteArray())

        return ResponseEntity(String(response.get().data), HttpStatus.CREATED)
    }
}

//    @PutMapping("/{id}")
//    fun updateChat(@PathVariable id: String, @RequestBody updatedMongoChat: MongoChat): ResponseEntity<Void> {
//        natsService.updateChat(ObjectId(id), updatedMongoChat)
//        return ResponseEntity(HttpStatus.OK)
//    }
//
//    @DeleteMapping("/{id}")
//    fun deleteChat(@PathVariable id: ObjectId): ResponseEntity<Void> {
//        natsService.deleteChat(id)
//        return ResponseEntity(HttpStatus.OK)
//    }
//
//    @GetMapping("/{id}")
//    fun findChatById(@PathVariable id: ObjectId): ResponseEntity<MongoChat?> {
//        val chat = natsService.findChatById(id)
//        return ResponseEntity(chat, HttpStatus.OK)
//    }
//
////    @GetMapping("/")
////    fun findAllChats(): ResponseEntity<List<MongoChat>> {
////        val chats = natsService.requestAllChats()
////        return ResponseEntity(chats, HttpStatus.OK)
////    }
//}
