package rys.ajaxpetproject.configuration

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import rys.ajaxpetproject.exception.BadIdTypeException
import rys.ajaxpetproject.exception.ChatNotFoundException
import rys.ajaxpetproject.exception.MessagesNotFoundException
import rys.ajaxpetproject.exception.UserNotFoundException
import rys.ajaxpetproject.exception.UserInChatNotFoundException
import rys.ajaxpetproject.exception.MessagesFromChatNotFoundException


@RestControllerAdvice
class GlobalRestExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(): ResponseEntity<Any> {
        val body = mapOf(
            "message" to "User Not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(BadIdTypeException::class)
    fun handleBadIdType(): ResponseEntity<Map<String, Any>> {
        val body = mapOf(
            "message" to "Bad Request",
            "status" to HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ChatNotFoundException::class)
    fun handleChatNotFound(): ResponseEntity<Any> {
        val body = mapOf(
            "message" to "Chat not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MessagesNotFoundException::class)
    fun handleMessagesNotFound(): ResponseEntity<Any> {
        val body = mapOf(
            "message" to "Message Not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MessagesFromChatNotFoundException::class)
    fun handleMessagesFromChatNotFound(): ResponseEntity<Any> {
        val body = mapOf(
            "message" to "Message In Chat Not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(UserInChatNotFoundException::class)
    fun handleUserInChatNotFound(): ResponseEntity<Any> {
        val body = mapOf(
            "message" to "User In Chat Not Found",
            "status" to HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationException(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val body = mapOf(
            "message" to  ex.bindingResult.allErrors.map { it.defaultMessage }
                .toString().let{it.substring(1,it.lastIndex)}.toString(),
            "status" to ex.statusCode.value()
        )
        return ResponseEntity(body, ex.statusCode)
    }
}
