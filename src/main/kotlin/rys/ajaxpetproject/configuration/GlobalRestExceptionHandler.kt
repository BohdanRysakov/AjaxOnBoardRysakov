package rys.ajaxpetproject.configuration

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import rys.ajaxpetproject.exception.*

@RestControllerAdvice
class GlobalRestExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("User not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(BadIdTypeException::class)
    fun handleBadIdType(ex: BadIdTypeException): ResponseEntity<Map<String, Any>> {
        val body = mapOf(
            "message" to "Bad Id Type",
            "status" to HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ChatNotFoundException::class)
    fun handleChatNotFound(ex: ChatNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("Chat not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MessagesNotFoundException::class)
    fun handleMessagesNotFound(ex: MessagesNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("Message not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MessagesFromChatNotFoundException::class)
    fun handleMessagesFromChatNotFound(ex: MessagesFromChatNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("Messages in Chat not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MessageNotFoundException::class)
    fun handleMessageNotFound(ex: MessageNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("Message not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(UsersNotFoundException::class)
    fun handleUsersNotFound(ex: UsersNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("Users not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(UserInChatNotFoundException::class)
    fun handleUserInChatNotFound(ex: UserInChatNotFoundException): ResponseEntity<Any> {
        return ResponseEntity("Users In Chat not found", HttpStatus.NOT_FOUND)
    }
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationException(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val body = mapOf(
            "message" to  ex.bindingResult.allErrors.map { it.defaultMessage }
                .toString().let{it.substring(1,it.lastIndex)}.toString(),
            "status" to ex.statusCode.value()
        )
        return ResponseEntity(
           body,
            ex.statusCode)
    }
}
