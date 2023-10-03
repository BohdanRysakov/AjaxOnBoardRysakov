package rys.nats.natsservice

import com.fasterxml.jackson.databind.ObjectMapper
import io.nats.client.Connection
import io.nats.client.Subscription
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import rys.rest.model.MongoChat
import rys.rest.repository.ChatRepository

@Service

class NatsChatService(private val natsConnection: Connection, private val chatRepository: ChatRepository) {

    private val objectMapper = ObjectMapper()


    private val subscription: Subscription = natsConnection.subscribe("chat") { msg ->
        val idRegex = "MongoChat\\(id=(\\w+),".toRegex()
        val idMatch = idRegex.find(String(msg.data))
        val nameRegex = "name=(\\w+),".toRegex()
        val nameMatch = nameRegex.find(String(msg.data))

//        val id = ObjectId(idMatch?.groups?.get(1)?.value)
        val name = nameMatch?.groups?.get(1)?.value
        val usersRegex = "users=\\[([^]]+)\\]".toRegex()
        val usersMatch = usersRegex.find(String(msg.data))
        val users: List<ObjectId> = usersMatch?.groups?.get(1)?.value?.split(", ")
            ?.map { ObjectId(it.trim()) } ?: listOf()



        val chat =  chatRepository.save(MongoChat(name = name, users = users))


            println(chat.name)
            println(chat.users)
            println("I got message")
        }


}
