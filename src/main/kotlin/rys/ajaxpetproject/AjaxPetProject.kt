package rys.ajaxpetproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["rys"])
class AjaxPetProject

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<AjaxPetProject>(*args)
}
