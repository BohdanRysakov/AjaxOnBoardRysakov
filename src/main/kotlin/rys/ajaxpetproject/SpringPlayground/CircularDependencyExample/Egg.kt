package rys.ajaxpetproject.SpringPlayground.CircularDependencyExample

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Egg {
    @Autowired
    lateinit var chicken: Chicken

    lateinit var size : String
    @PostConstruct
    fun init(){
/*
Example 1

 */
        println("Меня отложили!")
        chicken.action()

//        setSize()
//        println("My mom is ${chicken.color}")


    }

    fun action(){
       println("Сейчас вылуплюсь!")
    }
    fun setSize() {
        size="Big"
    }
}
