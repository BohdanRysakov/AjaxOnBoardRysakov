package rys.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<RestApplication>(*args)
}
