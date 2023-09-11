package rys.ajaxpetproject.model

import jakarta.validation.constraints.NotNull
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "MESSAGES")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
data class MongoMessage(
    @Id
    val id: ObjectId? = null,
    @field:NotNull(message = "message MUST be in chat")
    val chatId: ObjectId?,
    @field:NotNull(message = "message MUST have sender")
    val userId: ObjectId?,
    @field:NotNull(message = "message MUST have content")
    val content: String?,
    val sentAt: LocalDateTime = LocalDateTime.now())