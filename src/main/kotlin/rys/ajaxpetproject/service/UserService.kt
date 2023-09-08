package rys.ajaxpetproject.service

import org.bson.types.ObjectId
import rys.ajaxpetproject.model.MongoUser

interface UserService {
    fun createUser(mongoUser: MongoUser): MongoUser

    fun findUserById(id: ObjectId): MongoUser?

    fun getUserById(id: ObjectId): MongoUser

    fun findAllUsers(): List<MongoUser>?

    fun updateUser(id: ObjectId, updatedMongoUser: MongoUser): MongoUser?

    fun deleteUser(id: ObjectId): Boolean

    fun deleteUsers(): Boolean
}
