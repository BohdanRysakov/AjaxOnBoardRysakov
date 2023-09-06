package rys.ajaxpetproject.controller

import jakarta.validation.Valid
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
import rys.ajaxpetproject.model.Message
import rys.ajaxpetproject.service.MessageService
import java.util.*

@RestController
@RequestMapping("/messages")
class MessageController(val messageService: MessageService) {

    // Create a new Message
    @PostMapping("/")
    fun createMessage(@Valid @RequestBody message: Message): ResponseEntity<Message> =
         ResponseEntity(messageService.createMessage(message), HttpStatus.CREATED)

    @GetMapping("/{id}")
    fun getMessageById(@PathVariable id: UUID): ResponseEntity<Message> =
         ResponseEntity(messageService.getMessageById(id), HttpStatus.OK)

    @GetMapping("/chat/{chatId}")
    fun getAllMessagesByChatId(@PathVariable chatId: UUID): ResponseEntity<List<Message>> =
        ResponseEntity(messageService.getAllMessagesByChatId(chatId), HttpStatus.OK)

    @PutMapping("/{id}")
    fun updateMessage(@PathVariable id: UUID, @Valid @RequestBody updatedMessage: Message): ResponseEntity<Message> {
        val message = messageService.updateMessage(id, updatedMessage)
        return message?.let { ResponseEntity(it, HttpStatus.OK) } ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    // Delete a Message by its ID
    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: UUID): ResponseEntity<Boolean> {
        return if (messageService.deleteMessage(id)) {
            ResponseEntity(true, HttpStatus.OK)
        } else {
            ResponseEntity(false,HttpStatus.NOT_FOUND)
        }
    }
}
