package rys.ajaxpetproject.configuration

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class AspectLoggerController {

    @Pointcut("@within(rys.ajaxpetproject.annotation.Logging)" +
            " && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    fun loggingMethods() {
    }

    @Around("loggingMethods()")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)

        logger.info("ASPECT : ${joinPoint.signature.declaringType} going to invoke ${joinPoint.signature.name}")

        val t1: Long = System.currentTimeMillis()
        return try {
            joinPoint.proceed()
        } finally {
            logger.info("ASPECT : ${joinPoint.signature.declaringType} " +
                    "completed ${joinPoint.signature.name} in ${System.currentTimeMillis() - t1}ms")
        }
    }
}
