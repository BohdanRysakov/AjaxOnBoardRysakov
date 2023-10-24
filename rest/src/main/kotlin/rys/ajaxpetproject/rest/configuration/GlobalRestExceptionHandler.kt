package rys.ajaxpetproject.rest.configuration

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono
import rys.ajaxpetproject.exceptions.UserNotFoundException
import rys.ajaxpetproject.exceptions.ChatNotFoundException

@RestControllerAdvice
class GlobalRestExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(): Mono<ResponseEntity<Map<String, Any>>> {
        val body = mapOf(
            "message" to "User Not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return Mono.just(ResponseEntity(body, HttpStatus.NOT_FOUND))
    }

    @ExceptionHandler(ChatNotFoundException::class)
    fun handleChatNotFound(): Mono<ResponseEntity<Map<String, Any>>> {
        val body = mapOf(
            "message" to "Chat not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return Mono.just(ResponseEntity(body, HttpStatus.NOT_FOUND))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationException(ex: MethodArgumentNotValidException): Mono<ResponseEntity<Map<String, Any>>> {
        val body = mapOf(
            "message" to  ex.bindingResult.allErrors.map { it.defaultMessage }
                .toString().let{it.substring(1,it.lastIndex)}.toString(),
            // Assuming that `MethodArgumentNotValidException` has a method `statusCode` for fetching HTTP status.
            // Adjust as per your exact implementation.
            "status" to ex.statusCode.value()
        )
        return Mono.just(ResponseEntity(body, ex.statusCode))
    }
}
