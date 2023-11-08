package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ScanOptions
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.test.test
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.redis.repository.CacheChatRepository
import rys.ajaxpetproject.redis.repository.CacheMessageRepository
import rys.ajaxpetproject.repository.impl.UserRepositoryImpl
import rys.ajaxpetproject.internalapi.RedisPrefixes.CHAT_CACHE_KEY_PREFIX
import rys.ajaxpetproject.internalapi.RedisPrefixes.MESSAGE_CACHE_KEY_PREFIX

@DbIntegrationTest
@Suppress("LongMethod")
class ChatCacheRepositoryIT {

    @Autowired
    private lateinit var chatCacheRepository: CacheChatRepository

    @Autowired
    private lateinit var messageCacheRepository: CacheMessageRepository

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @Autowired
    private lateinit var redisOperations: ReactiveRedisTemplate<String, MongoChat>

    @BeforeEach
    fun init() {
        chatCacheRepository.deleteAll().block()
        messageCacheRepository.deleteAll().block()
        userRepository.deleteAll().block()
    }


    @Test
    fun `should return chat and cache him when chat found by id`() {
        //GIVEN
        val chat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
        )
        val expectedChat = chatCacheRepository.save(chat).block()!!
        val expectedChatId = expectedChat.id!!

        //WHEN //THEN
        chatCacheRepository.findChatById(expectedChatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(expectedChatId, actualChat.id)
                Assertions.assertEquals(expectedChat.name, actualChat.name)
                Assertions.assertNotNull(expectedChat.users)
                Assertions.assertNotNull(expectedChat.messages)
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$expectedChatId")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(expectedChat.id, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
            }
            .verifyComplete()
    }

    @Test
    fun `should return chat and cache him when chat saved`() {
        //GIVEN
        val expectedChatWithoutId = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        //WHEN //THEN
        chatCacheRepository.save(expectedChatWithoutId)
            .test()
            .expectSubscription()
            .assertNext { expectedChat ->
                Assertions.assertNotNull(expectedChat.id)
                Assertions.assertEquals(expectedChatWithoutId.name, expectedChat.name)
                Assertions.assertEquals(expectedChatWithoutId.users, expectedChat.users)
                Assertions.assertEquals(expectedChatWithoutId.messages, expectedChat.messages)
            }
            .verifyComplete()

        //THEN
        chatCacheRepository.findAll()
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertNotNull(actualChat.id)
                Assertions.assertEquals(expectedChatWithoutId.name, actualChat.name)
                Assertions.assertEquals(expectedChatWithoutId.users, actualChat.users)
                Assertions.assertEquals(expectedChatWithoutId.messages, actualChat.messages)
            }
            .verifyComplete()

        //THEN
        redisOperations.scan(
            ScanOptions.scanOptions()
                .match("$CHAT_CACHE_KEY_PREFIX*").build()
        )
            .flatMap {
                redisOperations.opsForValue().get(it)
            }
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(expectedChatWithoutId.name, cachedChat.name)
                Assertions.assertEquals(expectedChatWithoutId.users, cachedChat.users)
                Assertions.assertEquals(expectedChatWithoutId.messages, cachedChat.messages)
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit and clean cache when all chats deleted`() {
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

        chatCacheRepository.save(chat1).block()!!.id!!
        chatCacheRepository.save(chat2).block()!!.id!!
        chatCacheRepository.save(chat3).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.deleteAll()
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findAll()
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        redisOperations.scan(
            ScanOptions.scanOptions()
                .match("$CHAT_CACHE_KEY_PREFIX*").build()
        )
            .flatMap {
                redisOperations.opsForValue().get(it)
            }
            .test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return chat and save to cache when chat was updated`() {
        //GIVEN
        val expectedName = "UPDATED_NAME - ${System.nanoTime()}"
        val expectedUsers = listOf(ObjectId().toString(), ObjectId().toString())
        val expectedMessages = listOf(ObjectId().toString(), ObjectId().toString())

        val oldChat = MongoChat(
            name = "OLD_NAME - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val oldChatId = chatCacheRepository.save(oldChat).block()!!.id!!.toString()

        val expectedChat = MongoChat(
            id = oldChatId,
            name = expectedName,
            users = expectedUsers,
            messages = expectedMessages,
        )
        //WHEN //THEN
        chatCacheRepository.update(oldChatId, expectedChat)
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
        chatCacheRepository.findChatById(oldChatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(oldChatId, actualChat.id)
                Assertions.assertEquals(expectedName, actualChat.name)
                Assertions.assertEquals(expectedUsers, actualChat.users)
                Assertions.assertEquals(expectedMessages, actualChat.messages)
            }
            .verifyComplete()

        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX${expectedChat.id}")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(expectedChat.id, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
                Assertions.assertEquals(expectedChat.users, cachedChat.users)
                Assertions.assertEquals(expectedChat.messages, cachedChat.messages)
            }
            .verifyComplete()
    }

    @Test
    fun `should add user to list then return unit and update cache when addUser invoked`() {
        //GIVEN
        val expectedNewUserId = ObjectId().toString()
        val listOfUsers = mutableListOf(ObjectId().toString(), ObjectId().toString())
        val oldChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOfUsers,
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )
        listOfUsers.add(expectedNewUserId)

        val savedChatId = chatCacheRepository.save(oldChat).block()!!.id!!

        val expectedChat = MongoChat(
            id = savedChatId,
            name = oldChat.name,
            users = listOfUsers,
            messages = oldChat.messages,
        )

        //WHEN //THEN
        chatCacheRepository.addUser(expectedNewUserId, savedChatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findChatById(expectedChat.id!!)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(expectedChat.id!!, actualChat.id)
                Assertions.assertEquals(expectedChat.name, actualChat.name)
                Assertions.assertEquals(expectedChat.messages, actualChat.messages)
                Assertions.assertEquals(expectedChat.users.size, actualChat.users.size)
                Assertions.assertTrue(actualChat.users.contains(expectedNewUserId))
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX${expectedChat.id!!}")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(expectedChat.id, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
                Assertions.assertEquals(expectedChat.users, cachedChat.users)
                Assertions.assertEquals(expectedChat.messages, cachedChat.messages)
            }
            .verifyComplete()
    }

    @Test
    fun `should remove user from list then return unit and update cache when removeUser invoked`() {
        //GIVEN
        val unexpectedUser = ObjectId().toString()
        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString(), unexpectedUser),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatCacheRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.removeUser(unexpectedUser, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(expectedChat.name, actualChat.name)
                Assertions.assertEquals(expectedChat.messages, actualChat.messages)
                Assertions.assertEquals(expectedChat.users.size - 1, actualChat.users.size)
                Assertions.assertFalse(actualChat.users.contains(unexpectedUser))
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$chatId")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(chatId, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
                Assertions.assertEquals(expectedChat.messages, cachedChat.messages)
                Assertions.assertEquals(expectedChat.users.size - 1, cachedChat.users.size)
                Assertions.assertFalse(cachedChat.users.contains(unexpectedUser))
            }
            .verifyComplete()
    }

    @Test
    fun `should add message to list and return unit and update cache when addMessage is invoked`() {
        //GIVEN
        val expectedNewMessageId = ObjectId().toString()
        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatCacheRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.addMessage(expectedNewMessageId, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(expectedChat.name, actualChat.name)
                Assertions.assertEquals(expectedChat.users, actualChat.users)
                Assertions.assertEquals(expectedChat.messages.size + 1, actualChat.messages.size)
                Assertions.assertTrue(actualChat.messages.contains(expectedNewMessageId))
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$chatId")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(chatId, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
                Assertions.assertEquals(expectedChat.users, cachedChat.users)
                Assertions.assertEquals(expectedChat.messages.size + 1, cachedChat.messages.size)
                Assertions.assertTrue(cachedChat.messages.contains(expectedNewMessageId))
            }
            .verifyComplete()
    }

    @Test
    fun `should remove message from chat and return unit and update cache when removeMessage is invoked`() {
        //GIVEN
        val unexpectedMessageId = ObjectId().toString()
        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString(), unexpectedMessageId),
        )

        val chatId = chatCacheRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.removeMessage(unexpectedMessageId, chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .assertNext { actualChat ->
                Assertions.assertEquals(chatId, actualChat.id)
                Assertions.assertEquals(expectedChat.name, actualChat.name)
                Assertions.assertEquals(expectedChat.users, actualChat.users)
                Assertions.assertEquals(expectedChat.messages.size - 1, actualChat.messages.size)
                Assertions.assertFalse(actualChat.messages.contains(unexpectedMessageId))
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$chatId")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(chatId, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
                Assertions.assertEquals(expectedChat.users, cachedChat.users)
                Assertions.assertEquals(expectedChat.messages.size - 1, cachedChat.messages.size)
                Assertions.assertFalse(cachedChat.messages.contains(unexpectedMessageId))
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit and clean cache when chat is deleted`() {
        //GIVEN
        val unexpectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(ObjectId().toString(), ObjectId().toString()),
            messages = listOf(ObjectId().toString(), ObjectId().toString()),
        )

        val chatId = chatCacheRepository.save(unexpectedChat).block()!!.id!!

        //WHEN
        chatCacheRepository.delete(chatId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findChatById(chatId)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$chatId")
            .test()
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
            chatCacheRepository.save(expectedChat1).block()!!,
            chatCacheRepository.save(expectedChat2).block()!!,
            chatCacheRepository.save(expectedChat3).block()!!
        )

        //WHEN //THEN
        chatCacheRepository.findAll()
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
        chatCacheRepository.save(unexpectedChat).block()!!
        val expectedListOfChats = listOf(
            chatCacheRepository.save(expectedChat1).block()!!,
            chatCacheRepository.save(expectedChat2).block()!!,
            chatCacheRepository.save(expectedChat3).block()!!,
        )

        //WHEN //THEN
        chatCacheRepository.findChatsByUserId(expectedUserId)
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
        val unexpectedMessageId: String = messageCacheRepository.save(unexpectedMessage).block()!!.id!!

        val expectedListOfMessages = listOf(
            messageCacheRepository.save(expectedMessage1).block()!!,
            messageCacheRepository.save(expectedMessage2).block()!!,
            messageCacheRepository.save(expectedMessage3).block()!!,
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

        val expectedChatId = chatCacheRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.findMessagesByUserIdAndChatId(expectedUserId, expectedChatId)
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
            content = "message4 - ${System.nanoTime()}",
            userId = expectedUserId,
        )

        val expectedListOfMessages = listOf(
            messageCacheRepository.save(expectedMessage1).block()!!,
            messageCacheRepository.save(expectedMessage2).block()!!,
            messageCacheRepository.save(expectedMessage3).block()!!,
        )

        messageCacheRepository.save(unexpectedMessage).block()!!.id!!

        val expectedMessagesIds: List<String> = expectedListOfMessages.map { it.id!! }

        val expectedChat = MongoChat(
            id = null,
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(expectedUserId),
            messages = expectedMessagesIds,
        )

        val expectedChatId = chatCacheRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.findMessagesFromChat(expectedChatId)
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
    fun `should return unit and clean cache when messages from user is deleted`() {
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
        val expectedInitialMessage = MongoMessage(
            content = "message4 - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val unexpectedMessages = listOf(
            messageCacheRepository.save(unexpectedMessage1).block()!!,
            messageCacheRepository.save(unexpectedMessage2).block()!!,
            messageCacheRepository.save(unexpectedMessage3).block()!!,
        )

        val expectedMessage = messageCacheRepository.save(expectedInitialMessage).block()!!
        val expectedMessageList: List<MongoMessage> = listOf(expectedMessage)

        val unexpectedMessagesIds: List<String> = unexpectedMessages.map { it.id!! }

        val listOfAllMessages: List<String> = listOf(
            unexpectedMessagesIds[0],
            unexpectedMessagesIds[1],
            unexpectedMessagesIds[2],
            expectedMessage.id!!
        )

        val expectedChat = MongoChat(
            name = "chat1 - ${System.nanoTime()}",
            users = listOf(userId, ObjectId().toString()),
            messages = listOfAllMessages,
        )

        val expectedChatId = chatCacheRepository.save(expectedChat).block()!!.id!!

        //WHEN //THEN
        chatCacheRepository.deleteMessagesFromChatByUserId(expectedChatId, userId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        chatCacheRepository.findMessagesFromChat(expectedChatId)
            .test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedMessageList,
                    actualList
                )
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$CHAT_CACHE_KEY_PREFIX$expectedChatId")
            .test()
            .assertNext { cachedChat ->
                Assertions.assertEquals(expectedChatId, cachedChat.id)
                Assertions.assertEquals(expectedChat.name, cachedChat.name)
                Assertions.assertEquals(expectedChat.users, cachedChat.users)
                Assertions.assertEquals(expectedMessageList.size, cachedChat.messages.size)
                Assertions.assertTrue(cachedChat.messages.contains(expectedMessage.id))
                Assertions.assertFalse(cachedChat.messages.containsAll(unexpectedMessagesIds))
            }
            .verifyComplete()

        //THEN
        unexpectedMessagesIds.toFlux().flatMap {
            redisOperations.opsForValue().get("$MESSAGE_CACHE_KEY_PREFIX$it")
        }
            .test()
            .expectNextCount(0)
            .verifyComplete()
    }
}
