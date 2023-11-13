package rys.ajaxpetproject.chat.infrastructure.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration

@EnableAutoConfiguration
@SpringBootTest
@ContextConfiguration(
    classes = [
        MongoRepositoriesAutoConfiguration::class,
        DbIntegrationTest.DbIntegrationTestComponentScan::class,
    ]
)
annotation class DbIntegrationTest {

    @ComponentScan(
        value = [
            "rys.ajaxpetproject.repository",
            "rys.ajaxpetproject.chat.infrastructure.redis",
            "rys.ajaxpetproject.chat.infrastructure.mongo",
            "rys.ajaxpetproject.redis"
        ]
    )
    class DbIntegrationTestComponentScan
}
