package rys.ajaxpetproject.repository

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoUser

interface UserDao {
    fun save(mongoUser: MongoUser): MongoUser

    fun findUserById(id: ObjectId): MongoUser?

    fun findAllUsers(): List<MongoUser>

    fun deleteUserById(id: ObjectId): Boolean

    fun deleteAllUsers(): Boolean
}
