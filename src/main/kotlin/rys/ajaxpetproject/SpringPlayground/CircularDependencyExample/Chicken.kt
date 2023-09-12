package rys.ajaxpetproject.SpringPlayground.CircularDependencyExample

import jakarta.annotation.PostConstruct
import lombok.Getter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Chicken{
    @Autowired lateinit var egg: Egg

    @Getter
    lateinit var color : String

    @PostConstruct
    fun init(){
/*
Example 1

 */
          println("Я вылупился!")
          egg.action()
//
//        setColor()
//        println("My egg is ${egg.size}")

    }

    fun action() = println("Отложу-ка я яйцо")

    fun setColor() {
        color="Red"
    }

}
