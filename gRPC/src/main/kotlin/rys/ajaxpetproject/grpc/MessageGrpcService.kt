//package rys.ajaxpetproject.grpc
//
//import net.devh.boot.grpc.server.service.GrpcService
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import reactor.kotlin.core.publisher.toMono
//import rys.ajaxpetproject.request.message.create.proto.MessageCreateResponse
//import rys.ajaxpetproject.request.message.create.proto.MessageCreateRequest
//import rys.ajaxpetproject.service.ChatService
//import rys.ajaxpetproject.service.MessageService
//import rys.ajaxpetproject.service.message.ReactorMessageServiceGrpc
//import rys.ajaxpetproject.utils.toModel
//import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionRequest
//import rys.ajaxpetproject.request.message.subscription.proto.EventSubscription.CreateSubscriptionResponse
//import rys.ajaxpetproject.service.MessageEventService
//
//@GrpcService
//@Suppress("TooGenericExceptionCaught")
//class MessageGrpcService(
//    private val messageEventService: MessageEventService,
//    private val chatService: ChatService,
//    private val messageService: MessageService,
//) : ReactorMessageServiceGrpc.MessageServiceImplBase() {
//
//    override fun subscribe(request: CreateSubscriptionRequest): Flux<CreateSubscriptionResponse> {
//        logger.info("Received subscription request for chat {}", request.chatId)
//        return Flux.concat(
//            messageEventService.loadInitialState(request.chatId),
//            messageEventService.publishMessageCreatedEvent(request.chatId)
//        )
//            .onErrorResume { e -> Flux.just(buildFailureResponse(e.message)) }
//    }
//
//    private fun buildFailureResponse(message: String?): CreateSubscriptionResponse {
//        return CreateSubscriptionResponse.newBuilder().apply {
//            message?.let { failureBuilder.message = it }
//            failureBuilder.errorBuilder
//        }.build()
//    }
//
//    override fun create(request: MessageCreateRequest): Mono<MessageCreateResponse> {
//        return request.message.toModel().let {
//            messageService.create(it)
//        }.flatMap {
//            chatService.addMessage(it.id!!, request.chatId)
//        }
//            .then(createSuccessResponse().toMono())
//            .onErrorResume { createFailureResponse(it).toMono() }
//    }
//
//    private fun createSuccessResponse(): MessageCreateResponse {
//        return MessageCreateResponse.newBuilder().apply {
//            successBuilder.apply {
//                this.result = "Message created successfully"
//            }
//        }.build()
//    }
//
//    private fun createFailureResponse(e: Throwable): MessageCreateResponse {
//        return MessageCreateResponse.newBuilder().apply {
//            failureBuilder.apply {
//                this.message = e.message
//                this.errorBuilder
//            }
//        }.build()
//    }
//
//    companion object {
//        private val logger = org.slf4j.LoggerFactory.getLogger(MessageGrpcService::class.java)
//    }
//}
