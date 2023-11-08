package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ScanOptions
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.test.test
import reactor.test.StepVerifier
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.redis.repository.CacheMessageRepository
import rys.ajaxpetproject.internalapi.RedisPrefixes.MESSAGE_CACHE_KEY_PREFIX


@DbIntegrationTest
class MessageCacheRepositoryIT {
    @Autowired
    private lateinit var messageRepository: CacheMessageRepository

    @Autowired
    private lateinit var redisOperations: ReactiveRedisTemplate<String, MongoMessage>

    @BeforeEach
    fun init() {
        messageRepository.deleteAll().block()
    }

    @Test
    fun `should return message and save it to cache when message found by Id `() {
        // GIVEN
        val message = MongoMessage(
            content = "SUCCESFULL_findMessageById - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val expectedMessage = messageRepository.save(message).block()!!

        // WHEN //THEN
        messageRepository.findMessageById(expectedMessage.id!!.toString())
            .test()
            .expectSubscription()
            .assertNext { actualMessage ->
                Assertions.assertNotNull(actualMessage.id)
                Assertions.assertEquals(expectedMessage.content, actualMessage.content)
                Assertions.assertEquals(expectedMessage.userId, actualMessage.userId)
                Assertions.assertNotNull(actualMessage.sentAt)
            }
            .verifyComplete()

        //THEN
        redisOperations.scan(
            ScanOptions.scanOptions()
                .match("$MESSAGE_CACHE_KEY_PREFIX*").build()
        )
            .flatMap { redisOperations.opsForValue().get(it) }
            .test()
            .expectSubscription()
            .expectNext(expectedMessage)
            .verifyComplete()
    }

    @Test
    fun `should return empty and empty cache when findMessageById is called with invalid id`() {
        //GIVEN
        val invalidId = ObjectId().toString()

        // WHEN // THEN
        StepVerifier.create(messageRepository.findMessageById(invalidId))
            .expectSubscription()
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$MESSAGE_CACHE_KEY_PREFIX$invalidId")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return unit and clear cache when all messages deleted`() {
        //GIVEN
        val message1 = MongoMessage(
            content = "SUCCESFULL_deleteAll - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val message2 = MongoMessage(
            content = "SUCCESFULL_deleteAll - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val message3 = MongoMessage(
            content = "SUCCESFULL_deleteAll - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val unexpectedMessageId1 = messageRepository.save(message1).block()!!.id!!
        val unexpectedMessageId2 = messageRepository.save(message2).block()!!.id!!
        val unexpectedMessageId3 = messageRepository.save(message3).block()!!.id!!

        val expectedEmptyFlux = Mono.zip(
            messageRepository.findMessageById(unexpectedMessageId1),
            messageRepository.findMessageById(unexpectedMessageId2),
            messageRepository.findMessageById(unexpectedMessageId3)
        )

        //WHEN // THEN
        messageRepository.deleteAll()
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        expectedEmptyFlux
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()


        redisOperations.scan(
            ScanOptions.scanOptions()
                .match("$MESSAGE_CACHE_KEY_PREFIX*").build()
        )
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return message and update cache when update successful`() {
        //GIVEN
        val expectedContent = "SUCCESFULL_update - ${System.nanoTime()}"

        val oldMessage = MongoMessage(
            content = "OLD_MESSAGE_successful_update - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val oldMessageWithId = messageRepository.save(oldMessage).block()!!

        val expectedId = oldMessageWithId.id!!

        val expectedMessage = oldMessageWithId.copy(content = expectedContent)
        //WHEN //THEN
        messageRepository.update(expectedId, expectedMessage)
            .test()
            .expectSubscription()
            .assertNext { actualMessage ->
                Assertions.assertEquals(expectedId, actualMessage.id)
                Assertions.assertEquals(expectedContent, actualMessage.content)
                Assertions.assertEquals(oldMessage.userId, actualMessage.userId)
                Assertions.assertNotNull(actualMessage.sentAt)
            }
            .verifyComplete()

        //THEN
        messageRepository.findMessageById(expectedId)
            .test()
            .expectSubscription()
            .assertNext { actualMessage ->
                Assertions.assertEquals(expectedId, actualMessage.id)
                Assertions.assertEquals(expectedContent, actualMessage.content)
                Assertions.assertEquals(oldMessage.userId, actualMessage.userId)
                Assertions.assertNotNull(actualMessage.sentAt)
            }
            .verifyComplete()

        //THEN
        redisOperations.opsForValue().get("$MESSAGE_CACHE_KEY_PREFIX$expectedId")
            .test()
            .expectSubscription()
            .expectNext(expectedMessage.copy(id = expectedId))
            .verifyComplete()
    }

    @Test
    fun `should return unit when message deleted`() {
        //GIVEN
        val message = MongoMessage(
            content = "SUCCESFULL_delete - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val messageToDeleteId = messageRepository.save(message).block()!!.id!!

        //WHEN //THEN
        messageRepository.delete(messageToDeleteId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        messageRepository.findMessageById(messageToDeleteId)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return messages and update cache when multiple messages found by list of ids`() {
        //GIVEN
        val message1 = MongoMessage(
            content = "SUCCESFULL_findByIds - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val message2 = MongoMessage(
            content = "SUCCESFULL_findByIds - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val message3 = MongoMessage(
            content = "SUCCESFULL_findByIds - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val expectedMessage1 = messageRepository.save(message1).block()!!
        val expectedMessage2 = messageRepository.save(message2).block()!!
        val expectedMessage3 = messageRepository.save(message3).block()!!

        val expectedMessages = listOf(expectedMessage1, expectedMessage2, expectedMessage3)
        val expectedIds = expectedMessages.map { it.id!! }

        //WHEN //THEN
        messageRepository.findMessagesByIds(expectedIds)
            .test()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedMessages,
                    actualList
                )
            }
            .verifyComplete()

        //THEN
        expectedIds.toFlux()
            .flatMap {
                redisOperations.opsForValue().get("$MESSAGE_CACHE_KEY_PREFIX$it")
            }.test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertIterableEquals(
                    expectedMessages,
                    actualList
                )
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit and update cache when multiple messages deleted by ids `() {
        //GIVEN
        val message1 = MongoMessage(
            content = "SUCCESFULL_findByIds - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val message2 = MongoMessage(
            content = "SUCCESFULL_findByIds - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )
        val message3 = MongoMessage(
            content = "SUCCESFULL_findByIds - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val unexpectedMessage1 = messageRepository.save(message1).block()!!.id!!
        val unexpectedMessage2 = messageRepository.save(message2).block()!!.id!!
        val unexpectedMessage3 = messageRepository.save(message3).block()!!.id!!

        val idsToDelete = listOf(unexpectedMessage1, unexpectedMessage2, unexpectedMessage3)
        //WHEN //THEN
        messageRepository.deleteMessagesByIds(idsToDelete)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        messageRepository.findMessagesByIds(idsToDelete)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        //THEN
        idsToDelete.toFlux()
            .flatMap {
                redisOperations.opsForValue().get("$MESSAGE_CACHE_KEY_PREFIX$it")
            }.test()
            .expectSubscription()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                Assertions.assertEquals(0, actualList.size)
            }
            .verifyComplete()
    }
}
