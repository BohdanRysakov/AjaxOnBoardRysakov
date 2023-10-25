package rys.ajaxpetproject.rest.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.service.MessageService

@RestController
@RequestMapping("/messages")
class MessageController(val messageService: MessageService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    fun createMessage(@Valid @RequestBody mongoMessage: MongoMessage):
            Mono<MongoMessage> =
        messageService.create(mongoMessage)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/{id}")
    fun findMessageById(@PathVariable id: String): Mono<MongoMessage> =
        messageService.findMessageById(id)

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateMessage(
        @PathVariable id: String,
        @Valid @RequestBody updatedMongoMessage: MongoMessage
    ): Mono<MongoMessage> =
        messageService.update(id, updatedMongoMessage)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    fun deleteMessage(@PathVariable id: String): Mono<Unit> =
        messageService.delete(id)
}
