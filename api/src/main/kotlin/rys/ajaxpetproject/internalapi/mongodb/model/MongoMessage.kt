package rys.ajaxpetproject.internalapi.mongodb.model

import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
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
