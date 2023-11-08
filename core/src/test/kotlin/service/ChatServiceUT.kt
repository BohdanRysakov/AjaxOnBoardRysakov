package service

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
import rys.ajaxpetproject.kafka.MessageCreateEventProducer
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.ChatRepository
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.UserService
import rys.ajaxpetproject.service.impl.ChatServiceImpl

@ExtendWith(MockKExtension::class)
@Suppress("UnusedPrivateMember")
class ChatServiceUT {
    @MockK
    private lateinit var chatRepository: ChatRepository

    @MockK
    private lateinit var kafkaEventSender: MessageCreateEventProducer

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var messageService: MessageService

    @InjectMockKs
    private lateinit var chatService: ChatServiceImpl

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

        val chat = MongoChat(
            id = chatId,
            name = "chatName",
            users = listOf(userId),
            messages = listOf(ObjectId().toString(), ObjectId().toString(), ObjectId().toString())
        )

        val messagesIdByUser = listOf(ObjectId().toString(), ObjectId().toString(), ObjectId().toString())

        val messages = listOf(
            MongoMessage(
                id = messagesIdByUser[0],
                content = "text",
                userId = userId,
            ),
            MongoMessage(
                id = messagesIdByUser[1],
                content = "text",
                userId = userId,
            ),
            MongoMessage(
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

        //WHEN //THEN
        chatService.deleteAllFromUser(userId, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()
    }
}
