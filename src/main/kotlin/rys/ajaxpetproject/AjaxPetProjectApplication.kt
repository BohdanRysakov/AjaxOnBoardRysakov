package rys.ajaxpetproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import rys.ajaxpetproject.kafka.MessageCreateEventReceiver

@SpringBootApplication
class AjaxPetProjectApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<AjaxPetProjectApplication>(*args)
}
