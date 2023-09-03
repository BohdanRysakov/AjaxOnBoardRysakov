package rys.ajaxpetproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackages = arrayOf("rys"))

class AjaxPetProjectApplication {

}

fun main(args: Array<String>) {
    runApplication<AjaxPetProjectApplication>(*args)


}