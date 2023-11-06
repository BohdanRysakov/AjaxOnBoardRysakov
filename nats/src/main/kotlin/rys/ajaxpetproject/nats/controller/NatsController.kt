package rys.ajaxpetproject.nats.controller

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import io.nats.client.Connection
import io.nats.client.Message
import reactor.core.publisher.Mono

interface NatsController<RequestT : GeneratedMessageV3, ResponseT : GeneratedMessageV3> {

    val subject: String

    val connection: Connection

    val parser: Parser<RequestT>

    fun handle(request: RequestT): Mono<ResponseT>

    fun reply(msg: Message): Mono<ResponseT> = handle(parser.parseFrom(msg.data))
}
