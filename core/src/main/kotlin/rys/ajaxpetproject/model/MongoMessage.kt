package rys.ajaxpetproject.model

import com.mongodb.internal.connection.Time
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date

@Document(collection = "MESSAGES")
data class MongoMessage(
    @Id
    val id: String? = null,
    @field:NotNull(message = "message MUST be in chat")
    val userId: String,
    @field:NotNull(message = "message MUST have content")
    val content: String,
    val sentAt: Date = Date.from(Instant.now())
)
