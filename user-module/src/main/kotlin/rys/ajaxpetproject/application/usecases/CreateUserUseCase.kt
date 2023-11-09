package rys.ajaxpetproject.application.usecases

import reactor.core.publisher.Mono
import rys.ajaxpetproject.application.port.out.ICreateUserOutPort
import rys.ajaxpetproject.domain.entity.User

class CreateUserUseCase : ICreateUserOutPort {
    override fun save() : Mono<User> {
        TODO()
    }
}
