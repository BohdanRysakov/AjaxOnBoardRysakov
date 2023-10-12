package rys.ajaxpetproject.nats.config

import com.google.protobuf.GeneratedMessageV3
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import rys.ajaxpetproject.nats.controller.NatsController


@Component
class NatsControllerConfigurerPostProcessor : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean is NatsController<*, *>) {
            initializeNatsController(bean, bean.connection)
        }
        return bean
    }

    fun <RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3>
            initializeNatsController(controller: NatsController<RequestT, ResponseT>, connection: Connection) {
        connection.createDispatcher { message ->
            val parsedData = controller.parser.parseFrom(message.data)
            val response = controller.handle(parsedData)
            connection.publish(message.replyTo, response.toByteArray())
        }.apply { subscribe(controller.subject) }
    }
}
