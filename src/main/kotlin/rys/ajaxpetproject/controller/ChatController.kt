package rys.ajaxpetproject.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.ajaxpetproject.annotation.Logging
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.service.ChatService


@RestController
@RequestMapping("/chats")
@Logging
class ChatController(val chatService: ChatService) {

    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: MongoChat): ResponseEntity<MongoChat> =
        ResponseEntity(chatService.createChat(mongoChat), HttpStatus.CREATED)

    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: ObjectId): ResponseEntity<MongoChat> =
        ResponseEntity(chatService.findChatById(id), HttpStatus.OK)

    @GetMapping("/")
    fun findAllChats(): ResponseEntity<List<MongoChat>> {
        println(chatService)
        val list: List<MongoChat> = chatService.findAllChats()
        println(list)
        return ResponseEntity(list, HttpStatus.OK)
    }

    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: ObjectId, @Valid @RequestBody updatedMongoChat: MongoChat):
            ResponseEntity<MongoChat> =
        ResponseEntity(chatService.updateChat(id, updatedMongoChat), HttpStatus.OK)

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: ObjectId): ResponseEntity<Boolean> =
        ResponseEntity(chatService.deleteChat(id), HttpStatus.OK)
}
