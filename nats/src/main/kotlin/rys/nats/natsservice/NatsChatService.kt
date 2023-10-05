package rys.nats.natsservice

import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.nats.protostest.Mongochat
import rys.rest.model.MongoChat
import rys.rest.repository.ChatRepository

@Service
class NatsChatService(private val natsConnection: Connection,
                      private val chatRepository: ChatRepository
) {

    @PostConstruct
    fun init() {
        val dispatcher : Dispatcher = natsConnection.createDispatcher()
        dispatcher.subscribe("chat.create") { message ->
            println("Received message on subject ${message.subject}")
            println(message.data.decodeToString())
            val mongoChat = Mongochat.ChatCreateRequest.parser()
                .parseFrom(message.data)
            val newChat = chatRepository.save(mongoChat.toMongoChat())
            message.replyTo?.let { replySubject ->
                natsConnection.publish(replySubject, "Processed successfully!".toByteArray())
            }
        }
    }
}

private fun Mongochat.ChatCreateRequest.toMongoChat(): MongoChat {
    return MongoChat(
        id = if (this.id != "null" ) ObjectId(this.id) else null,
        name = this.name,
        users = this.usersList.map { ObjectId(it) }
    )
}

