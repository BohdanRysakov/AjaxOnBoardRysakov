package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import rys.ajaxpetproject.model.MongoUser

@Component
interface UserRepository : MongoRepository<MongoUser, String> {

    fun findUserByUserName(userName: String): MongoUser?

    fun findUserById(id: ObjectId): MongoUser?

    fun deleteUserById(id: ObjectId): Boolean

    fun findAllBy() : List<MongoUser>?

}
