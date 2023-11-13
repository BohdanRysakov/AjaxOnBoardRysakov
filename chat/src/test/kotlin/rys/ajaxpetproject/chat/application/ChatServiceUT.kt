package rys.ajaxpetproject.chat.application

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import rys.ajaxpetproject.chat.application.service.ChatService
import rys.ajaxpetproject.chat.application.service.MessageServiceKludge
import rys.ajaxpetproject.chat.domain.Chat
import rys.ajaxpetproject.chat.domain.Message
import rys.ajaxpetproject.chat.infrastructure.kafka.MessageAddedEventProducer
import rys.ajaxpetproject.chat.infrastructure.mongo.ChatRepository
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.service.UserService

@ExtendWith(MockKExtension::class)
@Suppress("UnusedPrivateMember")
class ChatServiceUT {
    @MockK
    private lateinit var chatRepository: ChatRepository

    @MockK
    private lateinit var kafkaEventSender: MessageAddedEventProducer

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var messageService: MessageServiceKludge

    @InjectMockKs
    private lateinit var chatService: ChatService

    @Test
    fun `should return unit when all messages by specific user deleted from chat `() {
        //GIVEN
        val userId = ObjectId().toString()
        val chatId = ObjectId().toString()

        val user = MongoUser(
            id = userId,
            userName = "userName",
            password = "password",
        )

        val chat = Chat(
            id = chatId,
            name = "chatName",
            users = listOf(userId),
            messages = listOf(ObjectId().toString(), ObjectId().toString(), ObjectId().toString())
        )

        val messagesIdByUser = listOf(ObjectId().toString(), ObjectId().toString(), ObjectId().toString())

        val messages = listOf(
            Message(
                id = messagesIdByUser[0],
                content = "text",
                userId = userId,
            ),
            Message(
                id = messagesIdByUser[1],
                content = "text",
                userId = userId,
            ),
            Message(
                id = messagesIdByUser[2],
                content = "text",
                userId = userId,
            )
        )

        every {
            chatRepository.findChatById(chatId)
        } returns chat.toMono()

        every { userService.getUserById(userId) } returns user.toMono()

        every { chatRepository.findMessagesFromChat(chatId) } returns messages.toFlux()

        every { messageService.delete(any()) } returns Unit.toMono()

        every { chatRepository.deleteMessagesFromChatByUserId(any(), any()) } returns Unit.toMono()

        //WHEN //THEN
        chatService.deleteAllFromUser(userId, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()
    }
}
