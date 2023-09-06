package rys.ajaxpetproject.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.anyOf
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID

@WebMvcTest
//@SpringBootTest
@RequiredArgsConstructor
class UserControllerTest {
    @MockBean
    lateinit var userService: UserService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun getUserSuccessfullyTest() {
        val testUser = User(
            UUID.randomUUID(), "testUser",
            BCryptPasswordEncoder().encode("testPassword")
        )

        `when`(userService.getUserById(testUser.id)).thenReturn(testUser)

        mockMvc.perform(
            get("/users/${testUser.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testUser.id.toString()))
            .andExpect(jsonPath("$.userName").value(testUser.userName))
            .andExpect(jsonPath("$.password").value(testUser.password))
            .andReturn()
    }

    @Test
    fun getUserFailDueToBadRequestTest() {
        val testUser = User(
            UUID.randomUUID(), "testUser",
            BCryptPasswordEncoder().encode("testPassword")
        )

        `when`(userService.getUserById(testUser.id)).thenReturn(testUser)

        mockMvc.perform(
            get("/users/SomeString")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun createUserFailDueToValidationTest() {
        val testUser = User(
            UUID.randomUUID(), "12",
            "www"
        )
        val objectMapper = ObjectMapper()

        mockMvc.perform(
            post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser))
        )
            .andExpectAll(
                jsonPath(
                    "message",
                    anyOf(
                        `is`(
                            "Password must be between 8 and 100 characters, " +
                                    "Username must be between 3 and 20 characters"
                        ),
                        `is`(
                            "Username must be between 3 and 20 characters," +
                                    " Password must be between 8 and 100 characters"
                        )
                    )
                ),
                jsonPath("status").value(400)
            )
            .andExpect(status().isBadRequest)

    }
}
