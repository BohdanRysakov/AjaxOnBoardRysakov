package rys.ajaxpetproject

import io.nats.client.Connection
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import rys.nats.protostest.Test
import rys.nats.utils.NatsMongoChatParser.serializeMongoChats
import rys.rest.repository.ChatRepository
import kotlin.concurrent.thread

@SpringBootApplication(scanBasePackages = ["rys"])
class AjaxPetProject

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    val context =  runApplication<AjaxPetProject>(*args)

    val repository = context.getBean(ChatRepository::class.java)
    val natsConnection = context.getBean(Connection::class.java)

    val list = repository.findAllBy()

    val resppnse = natsConnection.request(
        "chat.WriteAll", serializeMongoChats(list))

    println(resppnse)


}
