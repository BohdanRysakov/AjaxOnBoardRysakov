package rys.ajaxpetproject.repository.impl.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserDao

interface UserRepository : MongoRepository<MongoUser, String>, UserDao {

    override fun findUserById(id: ObjectId): MongoUser?

    override fun deleteUserById(id: ObjectId): Boolean

    override fun findAllUsers(): List<MongoUser>
}
