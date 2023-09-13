package rys.ajaxpetproject.configuration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import rys.ajaxpetproject.annotation.Logging
import rys.ajaxpetproject.controller.ChatController
import rys.ajaxpetproject.service.ChatService

@Configuration
class LoggingBeanPostProcessor : BeanPostProcessor, ApplicationContextAware {

    private lateinit var context: ApplicationContext

    val map: MutableMap<String, Class<Any>> = mutableMapOf()

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        bean.javaClass.getAnnotation(Logging::class.java)?.let {
            map.put(beanName, bean.javaClass)
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (map.containsKey(beanName)) {
            val beanT = bean as ChatController
            val factory: Enhancer = Enhancer()
            factory.setSuperclass(map[beanName])
            context.getBean(beanName).javaClass.declaredMethods.forEach { method ->
                val logger = LoggerFactory.getLogger(map[beanName])
                logger.info("forEach loop - {}", method.name)
                if (method.isAnnotationPresent(GetMapping::class.java)) {

                    logger.info("Method touched : {}", method.name)
                    factory.setCallback(MethodInterceptor { obj, method, args, proxy ->
                        logger.info(
                            "Executing {} method from {}",
                            method.name, beanName
                        )
                        val result = proxy.invokeSuper(obj, args)
                        logger.info(
                            "Business logic have worked {} in {}",
                            method.name, beanName
                        )
                        result
                    }
                    )

                }
            }
            return factory.create(arrayOf(ChatService::class.java), arrayOf(bean.chatService))


        }
        return bean

    }
}
