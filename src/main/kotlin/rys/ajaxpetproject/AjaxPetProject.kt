package rys.ajaxpetproject

import io.nats.client.Connection
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import rys.nats.protostest.Test
import kotlin.concurrent.thread

@SpringBootApplication(scanBasePackages = ["rys"])
class AjaxPetProject

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    val context =  runApplication<AjaxPetProject>(*args)


//    val natsConnection : Connection = context.getBean("connection") as Connection
//    val FUCKINGSHIT = Test.testRequest.newBuilder().setId("1")
//    val byteArr = FUCKINGSHIT.build().toByteArray()
//    println("Start")
//    val test1 = Test.testRequest.parseFrom(byteArr)
//    println("Tsting : $test1 ")
//    val reply = natsConnection.publish("chat.create", byteArr)
//    println("Reply:$reply")
//
//    println("End")
}
