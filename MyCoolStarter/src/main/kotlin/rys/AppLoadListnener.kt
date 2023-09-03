package rys

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

class AppLoadListnener : ApplicationListener<ContextRefreshedEvent> {
    @Value("\${spring.application.name}")
    lateinit var appName: String
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        println("App is Up $appName")
    }
}