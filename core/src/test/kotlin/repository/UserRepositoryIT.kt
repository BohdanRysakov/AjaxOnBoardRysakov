package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository
import java.util.concurrent.atomic.AtomicInteger

@DbIntegrationTest
class UserRepositoryIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun init() {
        userRepository.deleteAll().block()
    }

    @Test
    fun `should return user by id when user with provided id exists`() {
        // Given
        val expectedName = "SUCESSFUL_findUserById - ${System.nanoTime()}"
        val expectedPassword = "SUCESSFUL_findUserById - ${System.nanoTime()}"
        val user = MongoUser(userName = expectedName, password = expectedPassword)
        val id = userRepository.save(user).block()!!.id!!.toString()

        // When & Then
        userRepository.findById(id)
            .test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should return user by name when user with provided name exists`() {
        // Given
        val expectedName = "Successful_findUserByName - ${System.nanoTime()}"
        val expectedPassword = "Successful_findUserByName - ${System.nanoTime()}"
        val user = MongoUser(userName = expectedName, password = expectedPassword)
        userRepository.save(user).block()

        // When & Then
        userRepository.findByName(expectedName)
            .test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should return nothing when no user with provided id exists`() {
        // Given
        val nonExistentId = ObjectId().toString()

        // When & Then
        userRepository.findById(nonExistentId)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return user when save method invoked`() {
        // Given
        val expectedUser = MongoUser(
            userName = "SUCESSFULL_save - ${System.nanoTime()}",
            password = "SUCESSFULL_password - ${System.nanoTime()}"
        )

        // When & Then
        userRepository.save(expectedUser)
            .test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertNotNull(actualUser.id)
                Assertions.assertEquals(expectedUser.userName, actualUser.userName)
                Assertions.assertEquals(expectedUser.password, actualUser.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should return user when update is successful`() {
        // Given
        val expectedName = "SUCESSFULL_update - ${System.nanoTime()}"
        val expectedPassword = "SUCESSFULL_password - ${System.nanoTime()}"

        val oldUser = MongoUser(
            userName = "OLD_USER_successful_update - ${System.nanoTime()}",
            password = "OLD_password - ${System.nanoTime()}"
        )
        val oldUserId = userRepository.save(oldUser).block()!!.id!!.toString()
        val expectedUser = MongoUser(
            userName = expectedName,
            password = expectedPassword
        )

        // When & Then
        userRepository.update(oldUserId, expectedUser)
            .test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(oldUserId, actualUser.id.toString())
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()

        //AND //THEN
        userRepository.findById(oldUserId)
            .test()
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(oldUserId, actualUser.id.toString())
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should return unit when user is deleted`() {
        //GIVEN
        val user = MongoUser(
            userName = "Successful_delete - ${System.nanoTime()}",
            password = "password"
        )
        val userToDeleteId = userRepository.save(user).block()!!.id!!.toString()

        //WHEN // THEN
        userRepository.delete(userToDeleteId)
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //AND //THEN
        userRepository.findById(userToDeleteId)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return all users in the collection`() {
        //GIVEN
        val counter = AtomicInteger(0)

        val user1 = MongoUser(
            userName = "SUCESSFULL_findAll1 - ${System.nanoTime()}",
            password = "SUCESSFULL_password - ${System.nanoTime()}"
        )
        val user2 = MongoUser(
            userName = "SUCESSFULL_findAll2 - ${System.nanoTime()}",
            password = "SUCESSFULL_password - ${System.nanoTime()}"
        )
        val user3 = MongoUser(
            userName = "SUCESSFULL_findAll3 - ${System.nanoTime()}",
            password = "SUCESSFULL_password3 - ${System.nanoTime()}"
        )
        val expectedUsersList: MutableList<MongoUser> = mutableListOf()
        expectedUsersList.add(userRepository.save(user1).block()!!)
        expectedUsersList.add(userRepository.save(user2).block()!!)
        expectedUsersList.add(userRepository.save(user3).block()!!)

        //WHEN //THEN
        userRepository.findAll()
            .test()
            .expectSubscription()
            .recordWith { ArrayList<MongoUser>() }
            .thenConsumeWhile(
                { counter.get() < expectedUsersList.size },
                { actualUser ->
                    Assertions.assertTrue(expectedUsersList.contains(actualUser))
                    counter.incrementAndGet()
                })
            .verifyComplete()

    }

    @Test
    fun `should not return any users when collection is empty`() {
        // WHEN //THEN
        userRepository.findAll()
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return unit when delete all worked`() {
        //GIVEN
        val user1 = MongoUser(
            userName = "SUCESSFULL_findAll1 - ${System.nanoTime()}",
            password = "SUCESSFULL_password - ${System.nanoTime()}"
        )
        val user2 = MongoUser(
            userName = "SUCESSFULL_findAll2 - ${System.nanoTime()}",
            password = "SUCESSFULL_password - ${System.nanoTime()}"
        )
        val user3 = MongoUser(
            userName = "SUCESSFULL_findAll3 - ${System.nanoTime()}",
            password = "SUCESSFULL_password3 - ${System.nanoTime()}"
        )
        val notExpectedUserList: MutableList<MongoUser> = mutableListOf()
        notExpectedUserList.add(userRepository.save(user1).block()!!)
        notExpectedUserList.add(userRepository.save(user2).block()!!)
        notExpectedUserList.add(userRepository.save(user3).block()!!)

        //WHEN // THEN
        userRepository.deleteAll()
            .test()
            .expectSubscription()
            .expectNext(Unit)
            .verifyComplete()

        //THEN
        userRepository.findAll()
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

    }
}
