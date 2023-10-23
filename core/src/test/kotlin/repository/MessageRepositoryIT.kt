package repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import rys.ajaxpetproject.repository.MessageRepository

@DbIntegrationTest
class MessageRepositoryIT {
    @Autowired
    private lateinit var messageRepository: MessageRepository

    @BeforeEach
    fun init() {
        messageRepository.deleteAll().block()
    }

    @Test
    fun `should return Mono of message when findMessageById invoked`() {
        // GIVEN

        // WHEN

        // THEN

    }

    @Test
    fun `should return empty Mono when findMessageById is called with invalid id`() {
        // GIVEN

        // WHEN

        // THEN

    }


}
