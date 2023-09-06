package rys.ajaxpetproject.model

import jakarta.validation.constraints.NotNull
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("messages")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
open class Message(
    @MongoId(value = FieldType.OBJECT_ID)
    open var id: UUID? = UUID.randomUUID(),
    @field:NotNull(message = "message MUST be in chat")
    open var chatId: UUID?,
    @field:NotNull(message = "message MUST have sender")
    open var userId: UUID?,
    @field:NotNull(message = "message MUST have content")
    open var content: String?,
    open var sentAt: LocalDateTime = LocalDateTime.now()
)
