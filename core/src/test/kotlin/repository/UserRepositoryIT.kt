package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier
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
        val id = userRepository.save(user).block()!!.id!!

        // When & Then
        StepVerifier.create(userRepository.findById(id))
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
        StepVerifier.create(userRepository.findByName(expectedName))
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
        val nonExistentId = ObjectId()

        // When & Then
        StepVerifier.create(userRepository.findById(nonExistentId))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return user when save method invoked`() {
        // Given
        val exptectedUser = MongoUser(
            userName = "SUCESSFULL_save - ${System.nanoTime()}",
            password = "SUCESSFULL_password - ${System.nanoTime()}"
        )

        // When & Then
        StepVerifier.create(userRepository.save(exptectedUser))
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertNotNull(actualUser.id)
                Assertions.assertEquals(exptectedUser.userName, actualUser.userName)
                Assertions.assertEquals(exptectedUser.password, actualUser.password)
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
        val oldUserId = userRepository.save(oldUser).block()!!.id!!
        val expectedUser = MongoUser(
            userName = expectedName,
            password = expectedPassword
        )

        // When & Then
        StepVerifier.create(userRepository.update(oldUserId, expectedUser))
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(oldUserId, actualUser.id)
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()

        //AND //THEN
        StepVerifier.create(userRepository.findById(oldUserId))
            .expectSubscription()
            .assertNext { actualUser ->
                Assertions.assertEquals(oldUserId, actualUser.id)
                Assertions.assertEquals(expectedName, actualUser.userName)
                Assertions.assertEquals(expectedPassword, actualUser.password)
            }
            .verifyComplete()
    }

    @Test
    fun `should return true when user is deleted`() {
        //GIVEN
        val user = MongoUser(
            userName = "Successful_delete - ${System.nanoTime()}",
            password = "password"
        )
        val userToDeleteId = userRepository.save(user).block()!!.id!!

        //WHEN // THEN
        StepVerifier.create(userRepository.delete(userToDeleteId))
            .expectSubscription()
            .expectNext(true)
            .verifyComplete()

        //AND //THEN
        StepVerifier.create(userRepository.findById(userToDeleteId))
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
        StepVerifier.create(userRepository.findAll())
            .expectSubscription()
            .recordWith { ArrayList<MongoUser>() }
            .thenConsumeWhile(
                {counter.get() <  expectedUsersList.size},
                { actualUser ->
                    Assertions.assertTrue(expectedUsersList.contains(actualUser))
                    counter.incrementAndGet()
                })
            .verifyComplete()

    }

    @Test
    fun `should not return any users when collection is empty`() {
        // WHEN //THEN
        StepVerifier.create(userRepository.findAll())
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}
