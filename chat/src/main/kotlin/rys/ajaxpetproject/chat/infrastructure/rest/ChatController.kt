package rys.ajaxpetproject.chat.infrastructure.rest

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
import rys.ajaxpetproject.chat.application.port.`in`.ChatServiceInPort
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message

@RestController
@RequestMapping("/chats")
@Suppress("TooManyFunctions")
class ChatController(val chatService: ChatServiceInPort) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    fun createChat(@RequestBody mongoChat: Chat): Mono<Chat> = chatService.save(mongoChat)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/{id}")
    fun findChatById(@PathVariable id: String): Mono<Chat> = chatService.findChatById(id)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/")
    fun findAllChats(): Flux<Chat> = chatService.findAll()

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    fun updateChat(
        @PathVariable id: String,
        @Valid @RequestBody updatedMongoChat: Chat
    ): Mono<Chat> = chatService.update(id, updatedMongoChat)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    fun deleteChat(@PathVariable id: String): Mono<Unit> = chatService.delete(id)

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{chatId}/users/{userId}")
    fun addUser(
        @PathVariable chatId: String,
        @PathVariable userId: String
    ): Mono<Unit> = chatService.addUser(userId, chatId).thenReturn(Unit)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{chatId}/users/{userId}")
    fun removeUser(
        @PathVariable chatId: String,
        @PathVariable userId: String
    ): Mono<Unit> = chatService.removeUser(userId, chatId)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/user/{userId}")
    fun findChatsByUserId(@PathVariable userId: String): Flux<Chat> =
        chatService.findChatsByUserId(userId)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/messages/user/{userId}/chat/{chatId}")
    fun findMessagesFromUser(
        @PathVariable userId: String,
        @PathVariable chatId: String
    ): Flux<Message> = chatService.getMessagesFromChatByUser(userId, chatId)

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping("/messages/chat/{chatId}")
    fun findMessagesInChat(@PathVariable chatId: String): Flux<Message> =
        chatService.getMessagesInChat(chatId)

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/messages/user/{userId}/chat/{chatId}")
    fun deleteAllFromUser(
        @PathVariable userId: String,
        @PathVariable chatId: String
    ): Mono<Unit> = chatService.deleteAllFromUser(userId, chatId)
}
