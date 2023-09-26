package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoUser

interface UserDAO {
    fun save(mongoUser: MongoUser): MongoUser

    fun getUserById(id: ObjectId): MongoUser?

    fun findAllUsers(): List<MongoUser>

    fun deleteUserById(id: ObjectId): Boolean

    fun deleteAllUsers(): Boolean
}
