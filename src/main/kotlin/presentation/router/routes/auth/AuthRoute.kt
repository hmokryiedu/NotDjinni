package not.djinni.presentation.router.routes.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.domain.repository.AuthRepository
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.auth.request.LoginRequest
import not.djinni.presentation.router.routes.auth.request.RegisterRequest
import not.djinni.presentation.router.routes.auth.resources.Auth
import org.koin.core.annotation.Single

@Single
class AuthRoute(private val authRepository: AuthRepository) : Route {

    override fun install(root: Routing) = with(root) {
        login()
        register()
    }

    private fun Routing.login() {
        post<Auth.Login> {
            val loginRequest = call.receive<LoginRequest>()
            authRepository
                .login(loginRequest.email, loginRequest.password)
                .onSuccess { call.respondText("Successfully logged in", status = HttpStatusCode.OK) }
                .onFailure { call.respondText(it.message.orEmpty(), status = HttpStatusCode.NotFound) }
        }
    }

    private fun Routing.register() {
        post<Auth.Register> {
            val registerRequest = call.receive<RegisterRequest>()
            authRepository
                .register(registerRequest.email, registerRequest.password)
                .onSuccess { call.respondText("Successfully registered", status = HttpStatusCode.OK) }
                .onFailure { call.respondText(it.message.orEmpty(), status = HttpStatusCode.NotFound) }
        }
    }
}