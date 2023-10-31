package rys.ajaxpetproject.kafka

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KafkaProducerController(private val kafkaProducer: KafkaProducer,
                              private val kafkaListener: KafkaListener) {

    @GetMapping("/kafka")
    fun produceMessage() {
        kafkaProducer.sendMessage("my-topic", null, "Simple message")
        kafkaListener.listen()
    }
}
