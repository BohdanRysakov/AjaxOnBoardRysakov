package rys.ajaxpetproject.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import rys.ajaxpetproject.model.User

@Component
interface UserRepository : MongoRepository<User, String> {

    fun findByUserName(userName : String) : User?

}
