package rys.ajaxpetproject.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import rys.ajaxpetproject.model.User
import rys.ajaxpetproject.repository.UserRepository

@RestController
class GreetingsController(@Autowired val userRepository: UserRepository) {

    @GetMapping("/{name}")
    fun sendGreeting(@PathVariable("name") name:String) : String {
        return "Hello ${name}"
    }

    @GetMapping("/get/{userName}")
    fun getUserInfoByName(@PathVariable("userName") userName:String) : User? {
        return userRepository.findByUserName(userName);
    }
}