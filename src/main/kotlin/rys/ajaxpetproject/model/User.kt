package rys.ajaxpetproject.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("users")
data class User(
    @Id
    val id: ObjectId = ObjectId(),
    val userName: String = "",

)