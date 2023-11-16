package rys.ajaxpetproject.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
commit-2
@Configuration
class SecurityConfiguration {
    @Bean commit-1-2
    fun getPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    commit-1
}
