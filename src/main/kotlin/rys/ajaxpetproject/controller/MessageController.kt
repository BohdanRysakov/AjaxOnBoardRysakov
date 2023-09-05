package rys.ajaxpetproject.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rys.ajaxpetproject.model.Message
import rys.ajaxpetproject.service.MessageService
import java.util.*

@RestController
@RequestMapping("/messages")
class MessageController(@Autowired val messageService: MessageService) {

    // Create a new Message
    @PostMapping("/")
    fun createMessage(@RequestBody message: Message): ResponseEntity<Message> {
        val newMessage = messageService.createMessage(message)
        return ResponseEntity(newMessage, HttpStatus.CREATED)
    }

    // Retrieve a single Message by its ID
    @GetMapping("/{id}")
    fun getMessageById(@PathVariable id: UUID): ResponseEntity<Message> {
        val message = messageService.getMessageById(id)
        return message?.let { ResponseEntity(it, HttpStatus.OK) } ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    // Retrieve all Messages for a specific Chat
    @GetMapping("/chat/{chatId}")
    fun getAllMessagesByChatId(@PathVariable chatId: UUID): ResponseEntity<List<Message>> {
        val messages = messageService.getAllMessagesByChatId(chatId)
        return ResponseEntity(messages, HttpStatus.OK)
    }

    // Update a Message by its ID
    @PutMapping("/{id}")
    fun updateMessage(@PathVariable id: UUID, @RequestBody updatedMessage: Message): ResponseEntity<Message> {
        val message = messageService.updateMessage(id, updatedMessage)
        return message?.let { ResponseEntity(it, HttpStatus.OK) } ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    // Delete a Message by its ID
    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: UUID): ResponseEntity<Boolean> {
        return if (messageService.deleteMessage(id)) {
            ResponseEntity(true, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}