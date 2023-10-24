package rys.ajaxpetproject.rest.controller

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
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.service.MessageService

@RestController
@RequestMapping("/messages")
class MessageController(val messageService: MessageService) {

    @PostMapping("/")
    fun createMessage(@Valid @RequestBody mongoMessage: MongoMessage): Mono<ResponseEntity<MongoMessage>> =
        messageService.create(mongoMessage)
            .map { message -> ResponseEntity(message, HttpStatus.CREATED) }

    @GetMapping("/{id}")
    fun findMessageById(@PathVariable id: ObjectId): Mono<ResponseEntity<MongoMessage>> =
        messageService.findMessageById(id)
            .map { message -> ResponseEntity(message, HttpStatus.OK) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @PutMapping("/{id}")
    fun updateMessage(
        @PathVariable id: ObjectId,
        @Valid @RequestBody updatedMongoMessage: MongoMessage
    ): Mono<ResponseEntity<MongoMessage>> =
        messageService.update(id, updatedMongoMessage)
            .map { message -> ResponseEntity(message, HttpStatus.OK) }

    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: ObjectId): Mono<ResponseEntity<Void>> =
        messageService.delete(id)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))
}
