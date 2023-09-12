package rys.ajaxpetproject.SpringPlayground.BPP

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Configuration
import rys.ajaxpetproject.annotation.Background
import rys.ajaxpetproject.annotation.PolicyType
import java.lang.reflect.Method

@Configuration
class BackgroundRunningMethodsBeanPostProcessor : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        bean.javaClass.declaredMethods.forEach {
            method -> method.getAnnotation(Background::class.java)?.let {
                    background ->
                when (background.Policy) {
                    PolicyType.INFINITE -> {
                        startBackgroundTask(
                            method,
                            bean,
                            delay = background.delay)
                    }
                    PolicyType.FINITE -> {
                        startBackgroundTask(
                            method,
                            bean,
                            delay = background.delay,
                            iterations = background.iterations)
                    }
                }
            }
        }
        return bean
    }

    fun startBackgroundTask(method:Method,bean :Any,delay:Long) {
        Thread{
            while (true) {
                method.invoke(bean)
                Thread.sleep(delay)
            }
        }.start()
    }

    fun startBackgroundTask(method:Method,bean :Any,delay:Long,iterations:Int) {
        Thread{
            repeat(iterations) {
                method.invoke(bean)
                Thread.sleep(delay)
            }
        }.start()
    }
}
