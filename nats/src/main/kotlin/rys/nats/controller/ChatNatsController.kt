package rys.nats.controller

import jakarta.annotation.PostConstruct
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rys.nats.natsservice.NatsService
import rys.rest.model.MongoChat

@RestController
@RequestMapping("/nats")
class ChatNatsController(private val natsService: NatsService) {

    @PostConstruct
    fun init() {
        println("Nats Controller Is up")
    }

    @PostMapping("/chat/")
    fun publishToNats(@RequestBody message: MongoChat) {
        println("Nats controller invoked")
        natsService.publish("chat", message.toString())
    }
}
