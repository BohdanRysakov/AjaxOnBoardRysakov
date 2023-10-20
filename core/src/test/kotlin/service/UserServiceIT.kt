package service

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import rys.ajaxpetproject.configuration.SecurityConfiguration
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.UserService
import rys.ajaxpetproject.service.impl.UserServiceReactiveImpl

//@SpringBootTest
//@ContextConfiguration(classes = [SecurityConfiguration::class])
@ExtendWith(MockKExtension::class)
class UserServiceIT {
    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

//    @Autowired
//    private lateinit var encoder: PasswordEncoder

    @Test
    fun `should return Mono of Created User when createUser() is invoked`() {
        // GIVEN
        val mongoUser = MongoUser(
            id = ObjectId("5f9c7b9b9d6b6e1d9c8b4567"),
            userName = "SUCCESSFUL_NAME - ${System.nanoTime()}",
            password = "SUCCESSFUL_PASSWORD - ${System.nanoTime()}"
        )
    //    val encodedPassword = encoder.encode(mongoUser.password)
        every {
            userRepository.save(mongoUser)
        } returns Mono.just(mongoUser)


        // WHEN
        val result: Mono<MongoUser> = userService.createUser(mongoUser)

        // THEN
        StepVerifier.create(result)
            .expectNext(mongoUser)
            .verifyComplete()
    }
}
