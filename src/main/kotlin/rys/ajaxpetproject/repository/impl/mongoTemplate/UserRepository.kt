package rys.ajaxpetproject.repository.impl.mongoTemplate

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import rys.ajaxpetproject.model.MongoUser
import rys.ajaxpetproject.repository.UserDao

@Repository
class UserRepository(private val mongoTemplate: MongoTemplate) : UserDao {

    private val clazz = MongoUser::class.java

    override fun save(mongoUser: MongoUser): MongoUser = mongoTemplate.save(mongoUser)

    override fun findUserById(id: ObjectId): MongoUser? = mongoTemplate.findById(id, clazz)

    override fun findAllUsers(): List<MongoUser> = mongoTemplate.findAll(clazz)

    override fun deleteUserById(id: ObjectId): Boolean {
        val result = mongoTemplate.remove(Query(Criteria.where("_id").`is`(id)), clazz)
        return result.deletedCount > 0
    }

    override fun deleteAllUsers(): Boolean {
        mongoTemplate.remove(Query(), clazz)
        return true
    }
}
