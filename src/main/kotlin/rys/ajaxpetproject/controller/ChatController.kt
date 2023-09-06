package rys.ajaxpetproject.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import rys.ajaxpetproject.model.Chat
import rys.ajaxpetproject.service.ChatService
import java.util.*

@RestController
@RequestMapping("/chats")
class ChatController(@Autowired val chatService: ChatService) {

    // Create a new Chat
    @PostMapping("/")
    fun createChat(@RequestBody chat: Chat): ResponseEntity<Chat> =
        ResponseEntity(chatService.createChat(chat), HttpStatus.CREATED)


    // Retrieve a single Chat by its ID
    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: UUID): ResponseEntity<Chat> =
        ResponseEntity(chatService.findChatById(id), HttpStatus.OK)

    @GetMapping("/")
    fun findAllChats(): ResponseEntity<List<Chat>> = ResponseEntity(chatService.findAllChats(), HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: UUID, @Valid @RequestBody updatedChat: Chat): ResponseEntity<Chat> =
        ResponseEntity(chatService.updateChat(id, updatedChat),HttpStatus.OK)

    // Delete a Chat by its ID
    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: UUID): ResponseEntity<Boolean> =
        ResponseEntity(chatService.deleteChat(id), HttpStatus.OK)

}

