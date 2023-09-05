package rys.ajaxpetproject.configuration

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.UserService


@Configuration
@EnableMongoRepositories(basePackageClasses = [UserRepository::class])
class MongoConfig {
    @Bean
    fun init(userService: UserService): CommandLineRunner {
        return CommandLineRunner {
            // Удаление всех существующих пользователей (опционально)
            userService.deleteUsers()

            // Заполнение базы данных тестовыми значениями
            val john = User(userName = "John", password = "pass1")
            val jane = User(userName = "Jane", password = "pass2")
            val mike = User(userName = "Mike", password = "pass2")


            userService.createUser(john)
            userService.createUser(jane)
            userService.createUser(mike)
        }
    }
}
