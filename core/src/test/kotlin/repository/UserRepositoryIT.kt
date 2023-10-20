package repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserRepository

@DbIntegrationTest
class UserRepositoryIT {
    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun init() {
        userRepository.deleteAll().block()
    }

    @Test
    fun `should return user when findById() success`() {
        //GIVEN
        val id = ObjectId("652f9203deb9044f309a7dd9")
        val user = MongoUser(
            id = id, userName = "Successful_findUserById - ${System.nanoTime()}",
            password = "password"
        )
        userRepository.save(user).block()

        //WHEN //THEN
        StepVerifier.create(userRepository.findById(id))
            .expectSubscription()
            .expectNext(user)
            .verifyComplete()
    }

    @Test
    fun `should return user when findByName() success`() {
        //GIVEN
        val givenName = "Successful_findUserById - ${System.nanoTime()}"
        val user = MongoUser(
            id = ObjectId("652f9203deb9044f309a7dd9"),
            userName = givenName,
            password = "password"
        )
        userRepository.save(user).block()

        //WHEN //THEN
        StepVerifier.create(userRepository.findByName(givenName))
            .expectSubscription()
            .expectNext(user)
            .verifyComplete()
    }

    @Test
    fun `should not return anything when findById() fails`() {
        //GIVEN
        val id = ObjectId("652f9203deb9044f309a7dd9")

        //WHEN //THEN
        StepVerifier.create(userRepository.findById(id))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `should return user when save() success`() {
        //GIVEN
        val user = MongoUser(
            userName = "Successful_save - ${System.nanoTime()}",
            password = "password"
        )

        //WHEN //THEN
        StepVerifier.create(userRepository.save(user))
            .expectSubscription()
            .expectNextMatches { u ->
                u.id != null && u.userName == user.userName && u.password == user.password
            }
            .verifyComplete()
    }

    @Test
    fun `should return user when update() success`() {
        //GIVEN
        val id = ObjectId("652f9203deb9044f309a7dd9")
        val oldUser = MongoUser(
            id = id,
            userName = "OLD_USER_successful_update - ${System.nanoTime()}",
            password = "OLD_password"
        )
        userRepository.save(oldUser).block()

        val newUser = MongoUser(
            userName = "NEW_USER_successful_update - ${System.nanoTime()}",
            password = "NEW_password"
        )

        //WHEN //THEN
        StepVerifier.create(userRepository.update(id, newUser))
            .expectSubscription()
            .expectNext(newUser.copy(id = id))
            .verifyComplete()

        //AND //THEN
        val actualUser = userRepository.findById(oldUser.id!!).block()
        Assertions.assertEquals(newUser.copy(id = oldUser.id), actualUser)
    }

    @Test
    fun `should return Mono of TRUE when delete() invoked`() {
        //GIVEN
        val id = ObjectId("652f9203deb9044f309a7dd9")
        val user = MongoUser(
            id = id,
            userName = "Successful_delete - ${System.nanoTime()}",
            password = "password"
        )
        userRepository.save(user).block()

        //WHEN //THEN
        StepVerifier.create(userRepository.delete(id))
            .expectSubscription()
            .expectNext(true)
            .verifyComplete()

        //AND //THEN
        val actualUser = userRepository.findById(id).block()
        Assertions.assertNull(actualUser)
    }

    @Test
    fun `should return Flux with All users in collection when findAll() is invoked`() {
        //GIVEN
        val id1 = ObjectId("652f9203deb9044f309a7dd7")
        val id2 = ObjectId("652f9203deb9044f309a7dd8")
        val id3 = ObjectId("652f9203deb9044f309a7dd9")


        val user1 = MongoUser(
            id = id1,
            userName = "Successful_findAll - ${System.nanoTime()}",
            password = "password"
        )
        val user2 = MongoUser(
            id = id2,
            userName = "Successful_findAll - ${System.nanoTime()}",
            password = "password"
        )
        val user3 = MongoUser(
            id = id3,
            userName = "Successful_findAll - ${System.nanoTime()}",
            password = "password"
        )
        userRepository.save(user1).block()
        userRepository.save(user2).block()
        userRepository.save(user3).block()

        //WHEN //THEN
        StepVerifier.create(userRepository.findAll())
            .expectSubscription()
            .expectNext(user1, user2, user3)
            .verifyComplete()
    }
    @Test
    fun `should not return anything when findAll() is invoked in empty collection`() {
        //WHEN //THEN
        StepVerifier.create(userRepository.findAll())
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}
