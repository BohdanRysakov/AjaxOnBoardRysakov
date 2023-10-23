package rys.ajaxpetproject.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "USERS")
data class MongoUser(
    @Id
    val id: ObjectId? = null,
    @field:NotNull(message = "Username cannot be null")
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val userName: String,
    @field:NotNull(message = "Password cannot be null")
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 3, message = "Password must be between 3 and 100 characters")
    val password: String,
)
