package rys.ajaxpetproject

import io.nats.client.Connection
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import rys.rest.repository.ChatRepository

@SpringBootApplication(scanBasePackages = ["rys"])
class AjaxPetProject

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    val context =  runApplication<AjaxPetProject>(*args)





}
