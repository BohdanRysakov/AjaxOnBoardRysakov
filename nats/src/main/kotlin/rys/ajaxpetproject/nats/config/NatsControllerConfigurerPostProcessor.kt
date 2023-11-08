package rys.ajaxpetproject.nats.config

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import rys.ajaxpetproject.nats.controller.NatsController
import rys.ajaxpetproject.nats.controller.ReactiveNatsHandler

@Component
class NatsControllerConfigurerPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean is NatsController<*, *>) {
            initializeNatsController(bean, bean.connection)
        }
        return bean
    }

    private fun <RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3>
            initializeNatsController(
        controller: NatsController<RequestT, ResponseT>,
        connection: Connection
    ) {
        val handler = ReactiveNatsHandler(controller)
        connection.createDispatcher(handler).apply { subscribe(controller.subject) }
    }
}
