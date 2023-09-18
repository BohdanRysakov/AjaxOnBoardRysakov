package rys.ajaxpetproject.configuration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import rys.ajaxpetproject.annotation.Logging
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation


@Configuration
class LoggerControllerBeanPostProcessor2 : BeanPostProcessor {

    private val map = mutableMapOf<String, KClass<*>>()
    private val beanToMethodMap = mutableMapOf<String, Set<Method>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass = bean::javaClass

        if (bean::class.java.isAnnotationPresent(Logging::class.java)) {
            println("BPP2 - Some really strange stuff happens here...")
            val setMethod = bean.javaClass.declaredMethods.asSequence()
                .filter { it.isAnnotationPresent(GetMapping::class.java) }
                .toSet()
            setMethod.let {
                if (it.isNotEmpty()) {
                    beanToMethodMap[beanName] = setMethod
                }
            }
            map[beanName] = bean::class
        }


        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val something = beanToMethodMap[beanName]?.let {
            println("BPP2 - ... am i supposed to be printed here? ....")
            val factory = Enhancer()
            factory.setSuperclass(map[beanName]!!.java)

            factory.setCallback(
                map[beanName]?.let { it1 -> interceptor(it1, bean) }
            )
            factory.create()

        }
        if(something != null){
            println("BPP2 - ... am i getting closer? ...")
            return something

        }


        return super.postProcessAfterInitialization(bean, beanName)

    }

    private fun interceptor(
        originClass: KClass<*>,
        bean: Any
    ): MethodInterceptor = MethodInterceptor { _, method, args, proxy ->
        val logger = LoggerFactory.getLogger(originClass.java)

        logger.info("BPP2 - ${originClass.java} going to invoke ${method.name}")
        val t1: Long = System.currentTimeMillis()
        try {
            method.invoke(bean)
        } finally {
            logger.info("BPP2 - ${originClass.java} completed ${method.name} in ${System.currentTimeMillis() - t1}ms")
        }
    }
}

