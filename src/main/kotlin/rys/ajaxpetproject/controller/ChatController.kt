package rys.ajaxpetproject.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.ajaxpetproject.model.Chat
import rys.ajaxpetproject.service.ChatService
import java.util.*

@RestController
@RequestMapping("/chats")
class ChatController(@Autowired val chatService: ChatService) {

    // Create a new Chat
    @PostMapping("/")
    fun createChat(@RequestBody chat: Chat): ResponseEntity<Chat> {
        val newChat = chatService.createChat(chat)
        return ResponseEntity(newChat, HttpStatus.CREATED)
    }

    // Retrieve a single Chat by its ID
    @GetMapping("/{id}")
    fun getChatById(@PathVariable id: UUID): ResponseEntity<Chat> {
        val chat = chatService.getChatById(id)
        return chat.let { ResponseEntity(it, HttpStatus.OK) }
    }

    // Get all Chats
    @GetMapping("/")
    fun getAllChats(): ResponseEntity<List<Chat>> {
        val chats = chatService.getAllChats()
        return ResponseEntity(chats, HttpStatus.OK)
    }

    // Update a Chat by its ID
    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: UUID, @RequestBody updatedChat: Chat): ResponseEntity<Chat> {
        val chat = chatService.updateChat(id, updatedChat)
        return chat?.let { ResponseEntity(it, HttpStatus.OK) } ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    // Delete a Chat by its ID
    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: UUID): ResponseEntity<Boolean> {
        return if (chatService.deleteChat(id)) {
            ResponseEntity(true, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
