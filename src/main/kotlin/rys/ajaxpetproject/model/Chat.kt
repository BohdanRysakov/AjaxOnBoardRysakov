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
import java.util.*

//todo dataclass
@Document("chats")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
open class Chat(
    @MongoId(value = FieldType.OBJECT_ID)
    open var id: UUID = UUID.randomUUID(),
    @field:NotNull(message = "Chat name cannot be null")
    @field:NotBlank(message = "Chat name cannot be blank")
    @field:Size(min = 3, max = 20, message = "Chat name must be between 3 and 20 characters")
    open var name: String,
    open var users: List<UUID>
)
