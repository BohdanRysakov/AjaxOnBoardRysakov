package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import reactor.test.StepVerifier
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.repository.MessageRepository

@DbIntegrationTest
class MessageRepositoryIT {
    @Autowired
    private lateinit var messageRepository: MessageRepository

    @BeforeEach
    fun init() {
        messageRepository.deleteAll().block()
    }

    @Test
    fun `should return message when message found by Id `() {
        // GIVEN
        val message = MongoMessage(
            content = "SUCCESFULL_findMessageById - ${System.nanoTime()}",
            userId = ObjectId().toString(),
        )

        val expectedMessage = messageRepository.save(message).block()!!

        // WHEN //THEN
        StepVerifier.create(messageRepository.findMessageById(expectedMessage.id!!.toString()))
            .expectSubscription()
            .assertNext { actualMessage ->
                Assertions.assertNotNull(actualMessage.id)
                Assertions.assertEquals(expectedMessage.content, actualMessage.content)
                Assertions.assertEquals(expectedMessage.userId, actualMessage.userId)
                Assertions.assertNotNull(actualMessage.sentAt)
            }
            .verifyComplete()
    }

    @Test
    fun `should return empty when findMessageById is called with invalid id`() {
        // WHEN // THEN
        StepVerifier.create(messageRepository.findMessageById(ObjectId().toString()))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return unit when all messages deleted`() {
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
    }

    @Test
    fun `should return message when update successful`() {
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
    fun `should return messages when multiple messages found by list of ids`() {
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
    }

    @Test
    fun `should return unit when multiple messages deleted by ids `() {
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
    }
}
