package rys.ajaxpetproject.configuration

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import rys.ajaxpetproject.model.Chat
import rys.ajaxpetproject.model.Message
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.ChatService
import rys.ajaxpetproject.service.MessageService
import rys.ajaxpetproject.service.UserService
import java.util.*


@Configuration
@EnableMongoRepositories(basePackageClasses = [UserRepository::class])
class MongoConfig {
    @Bean
    fun init( userService: UserService,  chatService: ChatService,  messageService: MessageService): CommandLineRunner {
        return CommandLineRunner {
            // Delete all existing data (optional)
            userService.deleteUsers()
            chatService.deleteChats()
            messageService.deleteMessages()

            // Create Users
            val users = listOf(
                User(userName = "Alice", password = "pass1"),
                User(userName = "Bob", password = "pass2"),
                User(userName = "Carol", password = "pass3"),
                User(userName = "Dave", password = "pass4"),
                User(userName = "Eve", password = "pass5")
            ).map { userService.createUser(it) }

            // Create Chats
            val chat1 = chatService.createChat(Chat(name = "Family", users = users.map { it.id }))
            val chat2 = chatService.createChat(Chat(name = "Friends", users = users.map { it.id }
                .subList(0, USERS_NUMBER_IN_CHAT1)))
            val chat3 = chatService.createChat(Chat(name = "Work", users = users.map { it.id }
                .subList(USERS_NUMBER_IN_CHAT1, USERS_NUMBER_IN_CHAT2)))

            // Create Messages
            val random = Random()

            for (chat in listOf(chat1, chat2, chat3)) {
                val messageCount = random.nextInt(MESSAGES_NUMBER_ADD) +
                       MESSAGES_NUMBER_MIN  // 10 to 30 messages

                for (i in 1..messageCount) {
                    val sender  = chat?.users?.get(random.nextInt(chat.users.size))
                    val content = "Message $i from $sender in ${chat?.name}"
                    messageService.createMessage(Message(chatId = chat?.id, userId = sender, content = content))
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
