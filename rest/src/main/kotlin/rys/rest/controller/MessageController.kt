package rys.rest.controller

import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.GetMapping
import rys.rest.model.MongoMessage
import rys.rest.service.MessageService

@RestController
@RequestMapping("/messages")
class MessageController(val messageService: MessageService) {

    @PostMapping("/")
    fun createMessage(@Valid @RequestBody mongoMessage: MongoMessage): ResponseEntity<MongoMessage> =
         ResponseEntity(messageService.createMessage(mongoMessage), HttpStatus.CREATED)

    @GetMapping("/{id}")
    fun getMessageById(@PathVariable id: ObjectId): ResponseEntity<MongoMessage> =
         ResponseEntity(messageService.findMessageById(id), HttpStatus.OK)

    @GetMapping("/chat/{chatId}")
    fun getAllMessagesByChatId(@PathVariable chatId: ObjectId): ResponseEntity<List<MongoMessage>> =
        ResponseEntity(messageService.findAllMessagesByChatId(chatId), HttpStatus.OK)

    @PutMapping("/{id}")
    fun updateMessage(@PathVariable id: ObjectId, @Valid @RequestBody updatedMongoMessage: MongoMessage):
            ResponseEntity<MongoMessage> {
        val message = messageService.updateMessage(id, updatedMongoMessage)
        return message?.let { ResponseEntity(it, HttpStatus.OK) } ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: ObjectId): ResponseEntity<Boolean> {
        return if (messageService.deleteMessage(id)) {
            ResponseEntity(true, HttpStatus.OK)
        } else {
            ResponseEntity(false, HttpStatus.NOT_FOUND)
        }
    }

}
