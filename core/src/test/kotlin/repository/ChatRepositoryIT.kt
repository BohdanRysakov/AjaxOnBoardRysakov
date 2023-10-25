package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.impl.ChatReactiveRepositoryImpl
import rys.ajaxpetproject.repository.impl.MessageReactiveRepository
import rys.ajaxpetproject.repository.impl.UserReactiveMongoRepository

@DbIntegrationTest
class ChatRepositoryIT {

    @Autowired
    private lateinit var chatRepository: ChatReactiveRepositoryImpl

    @Autowired
    private lateinit var messageRepository: MessageReactiveRepository

    @Autowired
    private lateinit var userRepository: UserReactiveMongoRepository

    @BeforeEach
    fun init() {
        chatRepository.deleteAll().block()
        messageRepository.deleteAll().block()
        userRepository.deleteAll().block()
    }


    @Test
    fun `should return chat when chat found by id`() {
        //GIVEN
        val chat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
        )
        val actualChat = chatRepository.save(chat).block()!!
        val actualChatId = actualChat.id!!

        //WHEN //THEN
        chatRepository.findChatById(actualChatId)
            .test()
            .expectSubscription()
            .assertNext { expectedChat ->
                Assertions.assertEquals(actualChatId, expectedChat.id)
                Assertions.assertEquals(chat.name, expectedChat.name)
                Assertions.assertNotNull(chat.users)
                Assertions.assertNotNull(chat.messages)
            }
            .verifyComplete()
    }

    @Test
    fun `should return chat when chat saved`() {
        //GIVEN
        val actualChatWithoutId = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        //WHEN //THEN
        chatRepository.save(actualChatWithoutId)
            .test()
            .expectSubscription()
            .assertNext { expectedChat ->
                Assertions.assertNotNull(expectedChat.id)
                Assertions.assertEquals(actualChatWithoutId.name, expectedChat.name)
                Assertions.assertEquals(actualChatWithoutId.users, expectedChat.users)
                Assertions.assertEquals(actualChatWithoutId.messages, expectedChat.messages)
            }
            .verifyComplete()

        //THEN
        chatRepository.findAll()
            .test()
            .expectSubscription()
            .assertNext { expectedChat ->
                Assertions.assertNotNull(expectedChat.id)
                Assertions.assertEquals(actualChatWithoutId.name, expectedChat.name)
                Assertions.assertEquals(actualChatWithoutId.users, expectedChat.users)
                Assertions.assertEquals(actualChatWithoutId.messages, expectedChat.messages)
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit when all chats deleted`() {
        //GIVEN
        val chat1 = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
        )
        val chat2 = MongoChat(
            name = "chat2",
        )
        val chat3 = MongoChat(
            name = "chat3",
        )

        chatRepository.save(chat1).block()!!.id!!
        chatRepository.save(chat2).block()!!.id!!
        chatRepository.save(chat3).block()!!.id!!

        //WHEN //THEN
        chatRepository.deleteAll()
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findAll()
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return chat when chat was updated`() {
        //GIVEN
        val expectedName = "UPDATED_NAME - ${System.nanoTime()}"
        val expectedUsers = listOf(ObjectId().toString(), ObjectId().toString())
        val expectedMessages = listOf(ObjectId().toString(), ObjectId().toString())

        val oldChat = MongoChat(
            name = "OLD_NAME - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val oldChatId = chatRepository.save(oldChat).block()!!.id!!.toString()

        val expectedChat = MongoChat(
            id = oldChatId,
            name = expectedName,
            users = expectedUsers,
            messages = expectedMessages,
        )
        //WHEN //THEN
        chatRepository.update(oldChatId, expectedChat)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(oldChatId, actualChat.id)
                Assertions.assertEquals(expectedName, actualChat.name)
                Assertions.assertEquals(expectedUsers, actualChat.users)
                Assertions.assertEquals(expectedMessages, actualChat.messages)
            }
            .verifyComplete()

        //THEN
        chatRepository.findChatById(oldChatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(oldChatId, actualChat.id)
                Assertions.assertEquals(expectedName, actualChat.name)
                Assertions.assertEquals(expectedUsers, actualChat.users)
                Assertions.assertEquals(expectedMessages, actualChat.messages)
            }
            .verifyComplete()
    }

    @Test
    fun `should add user to list and return unit when addUser invoked`() {
        //GIVEN
        val expectedNewUserId = ObjectId().toString()
        val chat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatRepository.save(chat).block()!!.id!!

        //WHEN //THEN
        chatRepository.addUser(expectedNewUserId, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(chat.name, actualChat.name)
                Assertions.assertEquals(chat.messages, actualChat.messages)
                Assertions.assertEquals(chat.users.size + 1, actualChat.users.size)
                Assertions.assertTrue(actualChat.users.contains(expectedNewUserId))
            }
            .verifyComplete()
    }

    @Test
    fun `should remove user from list and return unit when removeUser invoked`() {
        //GIVEN
        val unexpectedUser = ObjectId().toString()
        val chat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString(), unexpectedUser),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatRepository.save(chat).block()!!.id!!

        //WHEN //THEN
        chatRepository.removeUser(unexpectedUser, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(chat.name, actualChat.name)
                Assertions.assertEquals(chat.messages, actualChat.messages)
                Assertions.assertEquals(chat.users.size - 1, actualChat.users.size)
                Assertions.assertFalse(actualChat.users.contains(unexpectedUser))
            }
            .verifyComplete()
    }

    @Test
    fun `should add message to list and return unit when addMessage is invoked`() {
        //GIVEN
        val expectedNewMessageId = ObjectId().toString()
        val chat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatRepository.save(chat).block()!!.id!!

        //WHEN //THEN
        chatRepository.addMessage(expectedNewMessageId, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(chat.name, actualChat.name)
                Assertions.assertEquals(chat.users, actualChat.users)
                Assertions.assertEquals(chat.messages.size + 1, actualChat.messages.size)
                Assertions.assertTrue(actualChat.messages.contains(expectedNewMessageId))
            }
            .verifyComplete()
    }

    @Test
    fun `should remove message from chat and return unit when removeMessage is invoked`() {
        //GIVEN
        val unexpectedMessage = ObjectId().toString()
        val chat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString(), unexpectedMessage),
        )

        val chatId = chatRepository.save(chat).block()!!.id!!

        //WHEN //THEN
        chatRepository.removeMessage(unexpectedMessage, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(chat.name, actualChat.name)
                Assertions.assertEquals(chat.users, actualChat.users)
                Assertions.assertEquals(chat.messages.size - 1, actualChat.messages.size)
                Assertions.assertFalse(actualChat.messages.contains(unexpectedMessage))
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit when chat is deleted`() {
        //GIVEN
        val unexpectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatRepository.save(unexpectedChat).block()!!.id!!

        //WHEN
        chatRepository.delete(chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return list of all chats when findAll is invoked `() {
        //GIVEN
        val expectedChat1 = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        val expectedChat2 = MongoChat(
            name = "chat2",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        val expectedChat3 = MongoChat(
            name = "chat3",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val expectedListOfChats = listOf(
            chatRepository.save(expectedChat1).block()!!,
            chatRepository.save(expectedChat2).block()!!,
            chatRepository.save(expectedChat3).block()!!
        )

        //WHEN //THEN
        chatRepository.findAll()
            .test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoChat>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedListOfChats,
                    actualList
                )
            }
            .verifyComplete()
    }

    @Test
    fun `should return all user's chats when findChatsByUserId is invoked`() {
        //GIVEN
        val expectedUserId = ObjectId().toString()
        val expectedChat1 = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(expectedUserId, ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        val expectedChat2 = MongoChat(
            name = "chat2 = ${System.nanoTime()}",
            users = listOf(expectedUserId, ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        val expectedChat3 = MongoChat(
            name = "chat3 - ${System.nanoTime()}",
            users = listOf(expectedUserId, ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        val unexpectedChat = MongoChat(
            name = "chat3 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        chatRepository.save(unexpectedChat).block()!!
        val expectedListOfChats = listOf(
            chatRepository.save(expectedChat1).block()!!,
            chatRepository.save(expectedChat2).block()!!,
            chatRepository.save(expectedChat3).block()!!,
        )

        //WHEN //THEN
        chatRepository.findChatsByUserId(expectedUserId)
            .test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoChat>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedListOfChats,
                    actualList
                )
            }
            .verifyComplete()
    }

    @Test
    fun `should return list of messages by chat and user when findMessagesByUserIdAndChatId is invoked`() {
        //GIVEN
        val expectedUserId = ObjectId().toString()

        val expectedMessage1 = MongoMessage(
            content = "message1 - ${System.nanoTime()}",
            userId = expectedUserId,
        )
        val expectedMessage2 = MongoMessage(
            content = "message2 - ${System.nanoTime()}",
            userId = expectedUserId,
        )
        val expectedMessage3 = MongoMessage(
            content = "message3 - ${System.nanoTime()}",
            userId = expectedUserId,
        )
        val unexpectedMessage = MongoMessage(
            content = "message3 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val unexpectedMessageId: String = messageRepository.save(unexpectedMessage).block()!!.id!!

        val expectedListOfMessages = listOf(
            messageRepository.save(expectedMessage1).block()!!,
            messageRepository.save(expectedMessage2).block()!!,
            messageRepository.save(expectedMessage3).block()!!,
        )
        val expectedMessagesIds: List<String?> = expectedListOfMessages.map { it.id!! }

        val listOfAllMessages: List<String> = listOf(
            expectedMessagesIds[0]!!,
            expectedMessagesIds[1]!!,
            expectedMessagesIds[2]!!,
            unexpectedMessageId
        )

        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(expectedUserId, ObjectId().toString()),
            messages = listOfAllMessages,
        )

        val expectedChatId = chatRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatRepository.findMessagesByUserIdAndChatId(expectedUserId, expectedChatId)
            .test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedListOfMessages,
                    actualList
                )
            }
            .verifyComplete()
    }

    @Test
    fun `should return list of messages when findMessagesFromChat is invoked`() {
        //GIVEN
        val expectedMessage1 = MongoMessage(
            content = "message1 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val expectedMessage2 = MongoMessage(
            content = "message2 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val expectedMessage3 = MongoMessage(
            content = "message3 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val unexpectedMessage = MongoMessage(
            content = "message3 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val expectedListOfMessages = listOf(
            messageRepository.save(expectedMessage1).block()!!,
            messageRepository.save(expectedMessage2).block()!!,
            messageRepository.save(expectedMessage3).block()!!,
        )

        messageRepository.save(unexpectedMessage).block()!!.id!!

        val expectedMessagesIds: List<String> = expectedListOfMessages.map { it.id!! }

        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = expectedMessagesIds,
        )

        val expectedChatId = chatRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatRepository.findMessagesFromChat(expectedChatId)
            .test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedListOfMessages,
                    actualList
                )
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit when messages from user is deleted`() {
        //GIVEN
        val userId = ObjectId().toString()

        val unexpectedMessage1 = MongoMessage(
            content = "message1 - ${System.nanoTime()}",
            userId = userId,
        )
        val unexpectedMessage2 = MongoMessage(
            content = "message2 - ${System.nanoTime()}",
            userId = userId,
        )
        val unexpectedMessage3 = MongoMessage(
            content = "message3 - ${System.nanoTime()}",
            userId = userId,
        )
        val message = MongoMessage(
            content = "message3 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val unexpectedMessages = listOf(
            messageRepository.save(unexpectedMessage1).block()!!,
            messageRepository.save(unexpectedMessage2).block()!!,
            messageRepository.save(unexpectedMessage3).block()!!,
        )

        val expectedMessage = messageRepository.save(message).block()!!
        val expectedMessageId: String = expectedMessage.id!!

        val unexpectedMessagesIds: List<String> = unexpectedMessages.map { it.id!! }

        val listOfAllMessages: List<String> = listOf(
            unexpectedMessagesIds[0],
            unexpectedMessagesIds[1],
            unexpectedMessagesIds[2],
            expectedMessageId
        )

        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(userId, ObjectId().toString()),
            messages = listOfAllMessages,
        )

        val expectedChatId = chatRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatRepository.deleteMessagesFromUser(userId, expectedChatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatRepository.findMessagesFromChat(expectedChatId)
            .test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    listOf(expectedMessage),
                    actualList
                )
            }
            .verifyComplete()

    }
}
