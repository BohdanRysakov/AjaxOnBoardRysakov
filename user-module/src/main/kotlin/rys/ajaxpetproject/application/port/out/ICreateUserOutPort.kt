package rys.ajaxpetproject.application.port.out

import reactor.core.publisher.Mono
import rys.ajaxpetproject.domain.entity.User

interface ICreateUserOutPort {
    fun save() : Mono<User>
}
