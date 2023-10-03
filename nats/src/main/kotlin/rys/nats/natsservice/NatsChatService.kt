package rys.nats.natsservice

import com.fasterxml.jackson.databind.ObjectMapper
import io.nats.client.Connection
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import rys.nats.utils.NatsMongoChatParser
import rys.rest.model.MongoChat
@Service
class NatsChatService(private val natsConnection: Connection) {

    fun createChat(mongoChat: MongoChat): MongoChat? {
        val reply = natsConnection.request("chat.create", mongoChat.toString().toByteArray(), 20000)
        return reply?.let { NatsMongoChatParser.parse(String(it.data)) }
    }

    fun updateChat(id: ObjectId, updatedMongoChat: MongoChat): MongoChat? {
        val message = "id:$id;data:${updatedMongoChat}"
        val reply = natsConnection.request("chat.update", message.toByteArray())
        return reply?.let { NatsMongoChatParser.parse(String(it.data)) }
    }

    fun deleteChat(id: ObjectId): Boolean {
        val reply = natsConnection.request("chat.delete", id.toString().toByteArray())
        return reply?.let { String(it.data) == "true" } ?: false
    }

    fun findChatById(id: ObjectId): MongoChat? {
        val reply = natsConnection.request("chat.findById", id.toString().toByteArray())
        return reply?.let { NatsMongoChatParser.parse(String(it.data)) }
    }

//    fun findAllChats(): List<MongoChat> {
//        val reply = natsConnection.request("chat.findAll", "", Duration.ofSeconds(2))
//        // Assuming reply data is a JSON list of MongoChat
//        val typeRef = object : TypeReference<List<MongoChat>>() {}
//        return ObjectMapper().readValue(reply.data, typeRef)
//    }
}
