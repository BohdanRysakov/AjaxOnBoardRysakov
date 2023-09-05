package rys.ajaxpetproject.model

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("chats")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
open class Chat(
    @MongoId(value = FieldType.OBJECT_ID)
    open var id: UUID = UUID.randomUUID(),
    open var name: String,
    open var users: List<UUID> // IDs of users in this chat
)