package rys.ajaxpetproject.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.service.ChatService

@RestController
@RequestMapping("/chats")
class ChatController(val chatService: ChatService) {

    // Create a new Chat
    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: MongoChat): ResponseEntity<MongoChat> =
        ResponseEntity(chatService.createChat(mongoChat), HttpStatus.CREATED)


    // Retrieve a single Chat by its ID
    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: ObjectId): ResponseEntity<MongoChat> =
        ResponseEntity(chatService.findChatById(id), HttpStatus.OK)

    @GetMapping("/")
    fun findAllChats(): ResponseEntity<List<MongoChat>> = ResponseEntity(chatService.findAllChats(), HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: ObjectId, @Valid @RequestBody updatedMongoChat: MongoChat):
            ResponseEntity<MongoChat> =
        ResponseEntity(chatService.updateChat(id, updatedMongoChat),HttpStatus.OK)

    // Delete a Chat by its ID
    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: ObjectId): ResponseEntity<Boolean> =
        ResponseEntity(chatService.deleteChat(id), HttpStatus.OK)

}

