package rys.ajaxpetproject.rest.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.service.ChatService

@RestController
@RequestMapping("/chats")
@Suppress("TooManyFunctions")
class ChatController(val chatService: ChatService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: MongoChat): Mono<MongoChat> =
        chatService.save(mongoChat)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: String): Mono<MongoChat> =
        chatService.findChatById(id)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/")
    fun findAllChats(): Flux<MongoChat> = chatService.findAll()

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateChat(
        @PathVariable id: String,
        @Valid @RequestBody updatedMongoChat: MongoChat
    ): Mono<MongoChat> = chatService.update(id, updatedMongoChat)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: String): Mono<Unit> = chatService.delete(id)

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{chatId}/users/{userId}")
    fun addUser(
        @PathVariable chatId: String,
        @PathVariable userId: String
    ): Mono<ResponseEntity<Void>> =
        chatService.addUser(userId, chatId)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{chatId}/users/{userId}")
    fun removeUser(
        @PathVariable chatId: String,
        @PathVariable userId: String
    ): Mono<Unit> =
        chatService.removeUser(userId, chatId)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/chats/user/{userId}")
    fun findChatsByUserId(@PathVariable userId: String): Flux<MongoChat> =
        chatService.findChatsByUserId(userId)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/messages/user/{userId}/chat/{chatId}")
    fun findMessagesFromUser(
        @PathVariable userId: String,
        @PathVariable chatId: String
    ): Flux<MongoMessage> =
        chatService.findMessagesFromUser(userId, chatId)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/messages/chat/{chatId}")
    fun findMessagesInChat(@PathVariable chatId: String): Flux<MongoMessage> =
        chatService.findMessagesInChat(chatId)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/messages/user/{userId}/chat/{chatId}")
    fun deleteAllFromUser(
        @PathVariable userId: String,
        @PathVariable chatId: String
    ): Mono<Unit> =
        chatService.deleteAllFromUser(userId, chatId)
}
