package not.djinni.presentation.router.routes.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.auth.TokenProvider
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.auth.mapper.toStatusCode
import not.djinni.presentation.router.routes.auth.request.LoginRequest
import not.djinni.presentation.router.routes.auth.resources.Auth
import not.djinni.presentation.router.routes.auth.response.TokenResponse
import not.djinni.presentation.router.routes.common.mapper.toErrorResult
import org.koin.core.annotation.Single

@Single
class AuthRoute(
    private val authRepository: AuthRepository,
    private val tokenProvider: TokenProvider
) : Route {

    override fun install(root: Routing) = with(root) {
        login()
        register()
    }

    private fun Routing.login() {
        post<Auth.Login> {
            val loginRequest = call.receive<LoginRequest>()
            authRepository
                .login(loginRequest.email, loginRequest.password)
                .onSuccess { userId ->
                    val token = tokenProvider.generate(userId)
                    call.respond(HttpStatusCode.OK, TokenResponse(token))
                }
                .onFailure {
                    val errorResult = it.toErrorResult(AuthException::toStatusCode)
                    call.respond(status = errorResult.code, message = errorResult.model)
                }
        }
    }

    private fun Routing.register() {
        post<Auth.Register> {
            val registerRequest = call.receive<LoginRequest>()
            authRepository
                .register(registerRequest.email, registerRequest.password)
                .onSuccess { userId ->
                    val token = tokenProvider.generate(userId)
                    call.respond(HttpStatusCode.OK, TokenResponse(token))
                }
                .onFailure {
                    val errorResult = it.toErrorResult(AuthException::toStatusCode)
                    call.respond(status = errorResult.code, message = errorResult.model)
                }
        }
    }
}