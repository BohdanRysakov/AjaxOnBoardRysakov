package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import rys.ajaxpetproject.configuration.SecurityConfiguration
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.impl.UserServiceReactiveImpl

@ExtendWith(MockKExtension::class)
class UserServiceUT {
    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var encoder: PasswordEncoder

    @InjectMockKs
    private lateinit var userService: UserServiceReactiveImpl


    @Test
    fun `should return Mono of Created User when createUser() is invoked`() {
        // GIVEN
        every { encoder.encode(any()) } returns "mockedHashedPassword"


        val expectedName = "SUCCESSFUL_NAME - ${System.nanoTime()}"
        val expectedPassword = "SUCCESSFUL_PASSWORD - ${System.nanoTime()}"

        val expectedUser = MongoUser(
            userName = expectedName,
            password = expectedPassword
        )

        val encodedPassword = encoder.encode(expectedUser.password)

        every {
            userRepository.save(expectedUser.copy(password = encodedPassword))
        } returns Mono.just(expectedUser)
        every {
            userRepository.findByName(expectedName)
        } returns Mono.empty()

        // WHEN
        val result: Mono<MongoUser> = userService.createUser(expectedUser)

        // THEN
        StepVerifier.create(result)
            .assertNext { user ->
                Assertions.assertEquals(expectedName, user.userName)
                Assertions.assertEquals(expectedPassword, user.password)
            }
            .verifyComplete()
    }


    fun `should throw when createUser() invoked with wrong password`() {
        // GIVEN

        val expectedName = "SUCCESSFUL_NAME - ${System.nanoTime()}"
        val expectedPassword = "1"

        val expectedUser = MongoUser(
            userName = expectedName,
            password = expectedPassword
        )
        val encodedPassword = encoder.encode(expectedUser.password)

        every {
            userRepository.save(expectedUser.copy(password = encodedPassword))
        } returns Mono.just(expectedUser)
        every {
            userRepository.findByName(expectedName)
        } returns Mono.just(expectedUser)

        // WHEN
        val result: Mono<MongoUser> = userService.createUser(expectedUser)

        // THEN
        StepVerifier.create(result)
            .assertNext { user ->
                Assertions.assertEquals(expectedName, user.userName)
                Assertions.assertEquals(expectedPassword, user.password)
            }
            .verifyComplete()
    }
}
