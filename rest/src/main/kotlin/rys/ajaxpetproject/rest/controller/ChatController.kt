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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.service.ChatService

@RestController
@RequestMapping("/chats")
@Suppress("TooManyFunctions")
class ChatController(val chatService: ChatService) {
    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: MongoChat): Mono<ResponseEntity<MongoChat>> =
        chatService.save(mongoChat)
            .map { chat -> ResponseEntity(chat, HttpStatus.CREATED) }

    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: ObjectId): Mono<ResponseEntity<MongoChat>> =
        chatService.findChatById(id)
            .map { chat -> ResponseEntity(chat, HttpStatus.OK) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @GetMapping("/")
    fun findAllChats(): Flux<MongoChat> = chatService.findAll()

    @PutMapping("/{id}")
    fun updateChat(@PathVariable id: ObjectId, @Valid @RequestBody updatedMongoChat: MongoChat):
            Mono<ResponseEntity<MongoChat>> =
        chatService.update(id, updatedMongoChat)
            .map { chat -> ResponseEntity(chat, HttpStatus.OK) }

    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: ObjectId): Mono<ResponseEntity<Void>> =
        chatService.delete(id)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))

    @PutMapping("/{chatId}/users/{userId}")
    fun addUser(
        @PathVariable chatId: ObjectId,
        @PathVariable userId: ObjectId
    ): Mono<ResponseEntity<Void>> =
        chatService.addUser(userId, chatId)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))

    @DeleteMapping("/{chatId}/users/{userId}")
    fun removeUser(
        @PathVariable chatId: ObjectId,
        @PathVariable userId: ObjectId
    ): Mono<ResponseEntity<Void>> =
        chatService.removeUser(userId, chatId)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))

    @GetMapping("/chats/user/{userId}")
    fun findChatsByUserId(@PathVariable userId: ObjectId): Flux<MongoChat> =
        chatService.findChatsByUserId(userId)

    @GetMapping("/messages/user/{userId}/chat/{chatId}")
    fun findMessagesFromUser(
        @PathVariable userId: ObjectId,
        @PathVariable chatId: ObjectId
    ): Flux<MongoMessage> =
        chatService.findMessagesFromUser(userId, chatId)

    @GetMapping("/messages/chat/{chatId}")
    fun findMessagesInChat(@PathVariable chatId: ObjectId): Flux<MongoMessage> =
        chatService.findMessagesInChat(chatId)

    @DeleteMapping("/messages/user/{userId}/chat/{chatId}")
    fun deleteAllFromUser(
        @PathVariable userId: ObjectId,
        @PathVariable chatId: ObjectId
    ): Mono<ResponseEntity<Void>> =
        chatService.deleteAllFromUser(userId, chatId)
            .thenReturn(ResponseEntity<Void>(HttpStatus.OK))
}
