package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import rys.ajaxpetproject.exceptions.UserAlreadyExistsException
import rys.ajaxpetproject.exceptions.UserNotFoundException
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository
import rys.ajaxpetproject.service.impl.UserServiceImpl

@ExtendWith(MockKExtension::class)
class UserServiceUT {
    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var encoder: PasswordEncoder

    @InjectMockKs
    private lateinit var userService: UserServiceImpl


    @Test
    fun `should return user when createUser() is invoked`() {
        // GIVEN
        every { encoder.encode(any()) } returns "mockedHashedPassword"


        val expectedName = "SUCCESSFUL_NAME - ${System.nanoTime()}"
        val password = "SUCCESSFUL_PASSWORD - ${System.nanoTime()}"
        val expectedPassword = "mockedHashedPassword"

        val user = MongoUser(
            userName = expectedName,
            password = password
        )

        val encodedPassword = encoder.encode(user.password)

        every {
            userRepository.save(user.copy(password = encodedPassword))
        } returns user.copy(id = ObjectId().toString(), password = encodedPassword).toMono()
        every {
            userRepository.findByName(expectedName)
        } returns Mono.empty()

        // WHEN
        val result: Mono<MongoUser> = userService.createUser(user)

        // THEN
        result.test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertNotNull(actualUser.id)
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should throw when createUser() invoked with wrong password`() {
        // GIVEN

        val expectedName = "SUCCESSFUL_NAME - ${System.nanoTime()}"
        val expectedPassword = "1"

        val expectedUser = MongoUser(
            userName = expectedName,
            password = expectedPassword
        )

        every {
            userRepository.findByName(expectedName)
        } returns Mono.just(expectedUser)

        // WHEN
        val result: Mono<MongoUser> = userService.createUser(expectedUser)

        // THEN
        result.test()
            .expectError(UserAlreadyExistsException::class.java)
            .verify()
    }

    @Test
    fun `should return user when user found by id`() {
        //GIVEN
        val expectedId = ObjectId().toString()
        val expectedName = "SUCCESSFUL_NAME - ${System.nanoTime()}"
        val expectedPassword = "SUCCESSFUL_PASSWORD - ${System.nanoTime()}"

        val expectedUser = MongoUser(
            id = expectedId,
            userName = expectedName,
            password = expectedPassword
        )

        every {
            userRepository.findById(expectedId)
        } returns expectedUser.toMono()

        //WHEN //THEN
        userService.findUserById(expectedId).test()
            .expectSubscription()
            .assertNext { user ->
                Assertions.assertEquals(expectedId, user.id.toString())
                Assertions.assertEquals(expectedName, user.userName)
                Assertions.assertEquals(expectedPassword, user.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should return empty when user with provided id is absent`() {
        //GIVEN
        val idToFind = ObjectId().toString()

        every {
            userRepository.findById(idToFind)
        } returns Mono.empty()

        //WHEN //THEN
        userService.findUserById(idToFind).test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should throw when getUserByName return empty`() {
        //GIVEN
        val nameToFind = "SUCCESSFUL_NAME - ${System.nanoTime()}"

        every {
            userRepository.findByName(nameToFind)
        } returns Mono.empty()

        //WHEN
        userService.getUserByName(nameToFind).test()
            .expectSubscription()
            .expectError(UserNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `should return user when updateUser successful`() {
        //GIVEN
        val idToUpdate = ObjectId().toString()
        val expectedName = "SUCCESSFUL_NAME - ${System.nanoTime()}"
        val expectedPassword = "mockedHashedPassword"

        val expectedUser = MongoUser(
            id = idToUpdate,
            userName = expectedName,
            password = expectedPassword
        )

        val userToPass = MongoUser(
            userName = expectedName,
            password = "UNEXPECTED_STRING"
        )

        val user = MongoUser(
            id = idToUpdate,
            userName = "FAILED_NAME - ${System.nanoTime()}",
            password = "FAILED_PASSWORD - ${System.nanoTime()}"
        )
        every { encoder.encode(any()) } returns "mockedHashedPassword"

        every {
            userRepository.findById(idToUpdate)
        } returns user.toMono()

        every {
            userRepository.update(idToUpdate, expectedUser)
        } returns expectedUser.toMono()

        //WHEN //THEN
        userService.updateUser(idToUpdate, userToPass).test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(idToUpdate, actualUser.id.toString())
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()
    }
}
