package rys.ajaxpetproject.configuration

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.repository.UserRepository


@Configuration
@EnableMongoRepositories(basePackageClasses = [UserRepository::class])
class MongoConfig {
    @Bean
    fun init(repository: UserRepository): CommandLineRunner {
        return CommandLineRunner {
            // Удаление всех существующих пользователей (опционально)
            repository.deleteAll()

            // Заполнение базы данных тестовыми значениями
            val john = User(userName = "John")
            val jane = User(userName = "Jane")
            val mike = User(userName = "Mike")

            repository.save(john)
            repository.save(jane)
            repository.save(mike)
        }
    }
}