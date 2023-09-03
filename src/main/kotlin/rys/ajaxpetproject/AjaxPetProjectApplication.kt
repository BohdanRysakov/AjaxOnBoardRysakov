package rys.ajaxpetproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication


@SpringBootApplication(scanBasePackages = arrayOf("rys"))

class AjaxPetProjectApplication {

}

fun main(args: Array<String>) {
//    val app = runApplication<AjaxPetProjectApplication>(*args)
    // https://stackoverflow.com/questions/8588984/java-awt-headlessexception-when-calling-joptionpane-showmessagedialog-in-backing
    val builder = SpringApplicationBuilder(AjaxPetProjectApplication::class.java)
    builder.headless(false)
    builder.run(*args)

}