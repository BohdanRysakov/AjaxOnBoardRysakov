package rys.ajaxpetproject.SpringPlayground.BPP

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import rys.ajaxpetproject.annotation.Background
import rys.ajaxpetproject.annotation.PolicyType

@Configuration
class BackgroundRunningMethodsBeanPostProcessor : BeanPostProcessor {

    /*
    !!!JUST TRIED!!!
    works.
     */
    @Autowired
    lateinit var taskExecutor: TaskExecutor

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        bean.javaClass.declaredMethods.forEach {
            method ->
            method.getAnnotation(Background::class.java)
                ?.let {
                    background ->
                when (background.Policy) {
                    PolicyType.INFINITE -> {
                        taskExecutor.execute {
                            while (true) {
                                method.invoke(bean)
                                Thread.sleep(background.delay)
                            }
                        }
                    }
                    PolicyType.FINITE -> {
                       /*
                       Tried
                           - taskExecutor.execute
                       Here, but he does only 5-8/10 iterations, I bet it's not work properly because ... dk
                       Need further investigation
                        */
                        val thread = Thread{
                            repeat(background.iterations) {
                                method.invoke(bean)
                                Thread.sleep(background.delay)
                                println("Iteration #$it")
                            }
                        }
                        thread.start()
                    }
                }
            }
        }
        return bean
    }
}
