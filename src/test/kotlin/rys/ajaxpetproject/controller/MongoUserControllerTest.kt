package rys.ajaxpetproject.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.RequiredArgsConstructor
import org.bson.types.ObjectId
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.service.UserService
import java.util.*

@WebMvcTest(UserController::class)
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
class MongoUserControllerTest {
    @MockBean
    lateinit var userService: UserService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun getUserSuccessfullyTest() {
        val testMongoUser = MongoUser(
            id = ObjectId(Date(1694093823L * 1000)),
            userName = "testUser",
            password = BCryptPasswordEncoder().encode("testPassword")
        )
        whenever(
            userService
                .findUserById(testMongoUser.id!!)
        )
            .thenReturn(testMongoUser)

        mockMvc.perform(
            get("/users/${testMongoUser.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testMongoUser.id.toString()))
            .andExpect(jsonPath("$.userName").value(testMongoUser.userName))
            .andExpect(jsonPath("$.password").value(testMongoUser.password))
            .andReturn()
    }

    @Test
    fun getUserFailDueToBadRequestTest() {
        val testMongoUser = MongoUser(
            userName = "testUser", password =
            BCryptPasswordEncoder().encode("testPassword")
        )

        `when`(userService.findUserById(testMongoUser.id as ObjectId)).thenReturn(testMongoUser)

        mockMvc.perform(
            get("/users/SomeString")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun createUserFailDueToValidationTest() {
        val testMongoUser = MongoUser(
            userName = "12",
            password = "www"
        )
        val objectMapper = ObjectMapper()

        mockMvc.perform(
            post("/users/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMongoUser))
        )
            .andExpectAll(
                jsonPath(
                    "message", anyOf(
                        equalTo(
                            "Password must be between 8 and 100 characters, " +
                                    "Username must be between 3 and 20 characters"
                        ), equalTo(
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
/*
package rys.ajaxpetproject.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.RequiredArgsConstructor
import org.bson.types.ObjectId
import org.hamcrest.Matchers.equalTo
import com.nhaarman.mockitokotlin2.whenever
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
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@WebMvcTest(UserController::class)
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
class MongoUserControllerTest {
    @MockBean
    lateinit var userService: UserService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun getUserSuccessfullyTest() {
        val testMongoUser = MongoUser(
            userName = "testUser",
            password = BCryptPasswordEncoder().encode("testPassword")
        )
        whenever(userService.findUserById(testMongoUser.id as ObjectId)).thenReturn(testMongoUser)

        mockMvc.perform(
            get("/users/${testMongoUser.id}").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(testMongoUser.id.toString()))
            .andExpect(jsonPath("$.userName").value(testMongoUser.userName))
            .andExpect(jsonPath("$.password").value(testMongoUser.password)).andReturn()
    }

    @Test
    fun getUserFailDueToBadRequestTest() {
        val testMongoUser = MongoUser(
            userName = "testUser",
            password = BCryptPasswordEncoder().encode("testPassword")
        )

        `when`(userService.findUserById(testMongoUser.id as ObjectId)).thenReturn(testMongoUser)

        mockMvc.perform(
            get("/users/SomeString").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun createUserFailDueToValidationTest() {
        val testMongoUser = MongoUser(
            userName = "12",
            password = "www"
        )
        val objectMapper = ObjectMapper()

        mockMvc.perform(
            post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMongoUser))
        ).andExpectAll(
                jsonPath(
                    "message", anyOf(
                        equalTo(
                            "Password must be between 8 and 100 characters, " +
                                    "Username must be between 3 and 20 characters"
                        ), equalTo(
                            "Username must be between 3 and 20 characters," +
                                    " Password must be between 8 and 100 characters"
                        )
                    )
                ), jsonPath("status").value(400)
            ).andExpect(status().isBadRequest)
    }
}

 */
