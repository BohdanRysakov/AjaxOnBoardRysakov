package rys.ajaxpetproject.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import rys.ajaxpetproject.model.User
import java.util.*

@Component
interface UserRepository : MongoRepository<User, UUID> {

    fun findUserByUserName(userName: String): User?
    fun findUserById(id: UUID): User?
    fun deleteUserById(id: UUID): Boolean
    fun findAllBy() : List<User>?

}
