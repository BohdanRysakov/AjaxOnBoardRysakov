package rys.nats.controller

import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.nats.natsservice.NatsChatService
import rys.rest.model.MongoChat

@RestController
@RequestMapping("/nats/chats")
class NatsChatController(private val natsService: NatsChatService) {

    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: MongoChat): ResponseEntity<MongoChat?> {
        val chat = natsService.createChat(mongoChat)
        return ResponseEntity(chat,HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: String, @RequestBody updatedMongoChat: MongoChat): ResponseEntity<Void> {
        natsService.updateChat(ObjectId(id), updatedMongoChat)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: ObjectId): ResponseEntity<Void> {
        natsService.deleteChat(id)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: ObjectId): ResponseEntity<MongoChat?> {
        val chat = natsService.findChatById(id)
        return ResponseEntity(chat, HttpStatus.OK)
    }

//    @GetMapping("/")
//    fun findAllChats(): ResponseEntity<List<MongoChat>> {
//        val chats = natsService.requestAllChats()
//        return ResponseEntity(chats, HttpStatus.OK)
//    }
}
