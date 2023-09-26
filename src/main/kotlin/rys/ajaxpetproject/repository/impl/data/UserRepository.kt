package rys.ajaxpetproject.repository.impl.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserDAO

//@Repository
interface UserRepository : MongoRepository<MongoUser, String>, UserDAO {

    fun findUserByUserName(userName: String): MongoUser?

    override fun getUserById(id: ObjectId): MongoUser?

    override fun deleteUserById(id: ObjectId): Boolean

    override fun findAllUsers(): List<MongoUser>
}
