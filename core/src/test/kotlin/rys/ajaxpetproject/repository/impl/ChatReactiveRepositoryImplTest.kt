package rys.ajaxpetproject.repository.impl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import repository.DbIntegrationTest
import rys.ajaxpetproject.model.MongoChat
import rys.ajaxpetproject.model.MongoMessage
import rys.ajaxpetproject.model.MongoUser

@DbIntegrationTest
class ChatReactiveRepositoryImplTest {

    @Autowired
    private lateinit var chatRepo: ChatReactiveRepositoryImpl

    @Autowired
    private lateinit var messageRepo: MessageReactiveRepository

    @Autowired
    private lateinit var userRepo: UserReactiveMongoRepository

    @BeforeEach
    fun init() {
        chatRepo.deleteAll().block()
        messageRepo.deleteAll().block()
        userRepo.deleteAll().block()
    }


    @Test
    fun findMessagesByUserIdAndChatId() {
        //GIVEN
        val user = MongoUser(
            userName = "user1",
            password = "password"
        )
        val actualUserId = userRepo.save(user).block()!!.id!!.toString()

        val message1 = MongoMessage(
            userId = actualUserId,
            content = "message1",
        )

        val message2 = MongoMessage(
            userId = actualUserId,
            content = "message2",
        )
        val message3 = MongoMessage(
            userId = actualUserId,
            content = "message3",
        )
        val actualMessage1 = messageRepo.save(message1).block()
        val actualMessage2 = messageRepo.save(message2).block()
        val actualMessage3 = messageRepo.save(message3).block()

        val messageId1 = actualMessage1!!.id!!.toString()
        val messageId2 = actualMessage2!!.id!!.toString()
        val messageId3 = actualMessage3!!.id!!.toString()


        val chat: MongoChat = MongoChat(
            name = "chat1",
            users = listOf(actualUserId),
            messages = listOf(messageId1, messageId2, messageId3)
        )
        val chatId = chatRepo.save(chat).block()!!.id!!.toString()

//        userRepo.findAll().subscribe { println(it) }
//        chatRepo.findMessagesFromChat(chatId).subscribe { println(it) }
//        println("Chat id : $chatId")
        println(userRepo.findById(actualUserId).block()!!)
        Thread.sleep(5000L)

        //WHEN //THEN
        chatRepo.findMessagesByUserIdAndChatId(actualUserId, chatId)
            .test()
            .recordWith { mutableListOf<MongoMessage>() }
            .thenConsumeWhile { true }
            .consumeRecordedWith { actualList ->
                println(actualList)
//                assertIterableEquals(
//                    listOf(actualMessage1, actualMessage2,actualMessage3),
//                    actualList
//                )
            }
            .verifyComplete()
    }
}
