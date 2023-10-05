package rys.rest.configuration

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import rys.rest.model.MongoChat
import rys.rest.model.MongoMessage
import rys.rest.model.MongoUser
import rys.rest.repository.ChatRepository
import rys.rest.service.ChatService
import rys.rest.service.MessageService
import rys.rest.service.UserService
import java.util.*

@Configuration
@EnableMongoRepositories(basePackageClasses = [ChatRepository::class])
@ComponentScan(basePackageClasses = [ChatRepository::class])
class MongoConfig {

    @Bean
    fun init(userService: UserService, chatService: ChatService, messageService: MessageService): CommandLineRunner {
        return CommandLineRunner {
            userService.deleteUsers()
            chatService.deleteChats()
            messageService.deleteMessages()

            val mongoUsers = listOf(
                MongoUser(userName = "Alice", password = "pass1"),
                MongoUser(userName = "Bob", password = "pass2"),
                MongoUser(userName = "Carol", password = "pass3"),
                MongoUser(userName = "Dave", password = "pass4"),
                MongoUser(userName = "Eve", password = "pass5")
            ).map { userService.createUser(it) }
            println(mongoUsers)

            val mongoChat1 = chatService.createChat(
                    MongoChat(name = "Lviv", users = mongoUsers.map { it.id })
                )

            val mongoChat2 = chatService.createChat(MongoChat(name = "Friends", users = mongoUsers.map { it.id }
                .subList(0, USERS_NUMBER_IN_CHAT1)))
            val mongoChat3 = chatService.createChat(MongoChat(name = "Work", users = mongoUsers.map { it.id }
                .subList(USERS_NUMBER_IN_CHAT1, USERS_NUMBER_IN_CHAT2)))
            for (chat in listOf(mongoChat1, mongoChat2, mongoChat3)) {
                val messageCount = Random().nextInt(MESSAGES_NUMBER_ADD) +
                        MESSAGES_NUMBER_MIN  // 10 to 30 messages

                for (i in 1..messageCount) {
                    val sender = chat.users[Random().nextInt(chat.users.size)]
                    val content = "Message $i from $sender in ${chat.name}"
                    messageService.createMessage(MongoMessage(chatId = chat.id, userId = sender, content = content))
                }
            }
        }
    }

    companion object {
        const val MESSAGES_NUMBER_MIN = 10
        const val MESSAGES_NUMBER_ADD = 21
        const val USERS_NUMBER_IN_CHAT1 = 3
        const val USERS_NUMBER_IN_CHAT2 = 5
    }
}
