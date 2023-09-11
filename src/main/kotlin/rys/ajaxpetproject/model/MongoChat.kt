package rys.ajaxpetproject.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "CHATS")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
data class MongoChat(
    @Id
    val id: ObjectId? = null,
    @field:NotNull(message = "Chat name cannot be null")
    @field:NotBlank(message = "Chat name cannot be blank")
    @field:Size(min = 3, max = 20, message = "Chat name must be between 3 and 20 characters")
    val name: String?,
    val users: List<ObjectId?>)
