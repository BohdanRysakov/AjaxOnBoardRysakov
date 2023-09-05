package rys.ajaxpetproject.model


import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.mapping.MongoId
import java.util.UUID



@Document("users")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
open class User(
    @field:MongoId(value = FieldType.OBJECT_ID)
    open var id: UUID = UUID.randomUUID(),
    @field:NotNull(message = "Username cannot be null")
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    open var userName: String = "",
    @field:NotNull(message = "Password cannot be null")
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, message = "Password must be between 8 and 100 characters")
    open var password: String = "",
)
