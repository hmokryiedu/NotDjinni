package not.djinni.presentation.router.routes.user

import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.domain.exception.user.UserException
import not.djinni.domain.repository.UserRepository
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.auth.JwtAuth.USER_ID_CLAIM_NAME
import not.djinni.presentation.router.routes.common.extension.getClaim
import not.djinni.presentation.router.routes.common.mapper.toErrorResult
import not.djinni.presentation.router.routes.user.mapper.toResponse
import not.djinni.presentation.router.routes.user.mapper.toStatusCode
import not.djinni.presentation.router.routes.user.resources.User
import org.koin.core.annotation.Single

@Single
class UserRoute(
    private val userRepository: UserRepository
) : Route {

    override fun install(root: Routing) = with(root) {
        getUser()
    }

    private fun Routing.getUser() {
        authenticate(JwtAuth.NAME) {
            get<User> {
                val userId = getClaim<Long>(USER_ID_CLAIM_NAME) ?: run {
                    val exception = UserException.NotFound()
                    val errorResult = exception.toErrorResult(UserException::toStatusCode)
                    call.respond(status = errorResult.code, message = errorResult.model)
                    return@get
                }
                userRepository.getUser(userId = userId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .onFailure {
                        val errorResult = it.toErrorResult(UserException::toStatusCode)
                        call.respond(status = errorResult.code, message = errorResult.model)
                    }
            }
        }
    }
}