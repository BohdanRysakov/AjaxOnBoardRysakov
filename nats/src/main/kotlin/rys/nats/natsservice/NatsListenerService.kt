package rys.nats.natsservice

import io.nats.client.Connection
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.nats.utils.NatsMongoChatParser
import rys.rest.repository.ChatRepository
@Service
class NatsListenerService(private val natsConnection: Connection, private val chatRepository: ChatRepository) {

    init {
        natsConnection.subscribe("chat.create") { msg ->
            val chat = NatsMongoChatParser.parse(String(msg.data))
            val savedChat = chat?.let { chatRepository.save(it) }
            natsConnection.publish(msg.replyTo, savedChat.toString().toByteArray())
        }

        natsConnection.subscribe("chat.update") { msg ->
            val data = String(msg.data).split(";")
            val id = ObjectId(data[0].split(":")[1])
            val chat = NatsMongoChatParser.parse(data[1].split(":")[1])
            val updatedChat = chat?.let { chatRepository.save(it) }
            natsConnection.publish(msg.replyTo, updatedChat.toString().toByteArray())
        }

        natsConnection.subscribe("chat.delete") { msg ->
            val id = ObjectId(String(msg.data))
            val deleted = chatRepository.existsById(id)
            if (deleted) {
                chatRepository.deleteById(id)
            }
            natsConnection.publish(msg.replyTo, deleted.toString().toByteArray())
        }

        natsConnection.subscribe("chat.findById") { msg ->
            val id = ObjectId(String(msg.data))
            val chat = chatRepository.findById(id).orElse(null)
            println("DFLKNCIPHJOJPWDKNQw")
            natsConnection.publish(msg.replyTo, chat.toString().toByteArray())
        }


//        natsConnection.subscribe("chat.findAll") { msg ->
//            val chats = chatRepository.findAll()
//            val jsonChats = ObjectMapper().writeValueAsString(chats)
//            natsConnection.publish(msg.replyTo, jsonChats)
//        }
    }
}
