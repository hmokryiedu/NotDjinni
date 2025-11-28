package not.djinni.presentation.router.routes.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.auth.mapper.toStatusCode
import not.djinni.presentation.router.routes.auth.request.LoginRequest
import not.djinni.presentation.router.routes.auth.request.RefreshTokenRequest
import not.djinni.presentation.router.routes.auth.request.RegisterRequest
import not.djinni.presentation.router.routes.auth.resources.Auth
import not.djinni.presentation.router.routes.auth.response.AuthResponse
import org.koin.core.annotation.Single

@Single
class AuthRoute(
    private val authRepository: AuthRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        login()
        register()
        refresh()
        logout()
    }

    private fun Routing.login() {
        post<Auth.Login> {
            val loginRequest = call.receive<LoginRequest>()
            authRepository.login(loginRequest.email, loginRequest.password)
                .mapCatching { id -> authRepository.generateTokens(id).getOrThrow() }
                .onSuccess { tokens ->
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = AuthResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
                    )
                }
                .handleError(call = call, mapToCode = AuthException::toStatusCode)
        }
    }

    private fun Routing.register() {
        post<Auth.Register> {
            val registerRequest = call.receive<RegisterRequest>()
            authRepository.register(registerRequest.name, registerRequest.email, registerRequest.password)
                .mapCatching { id -> authRepository.generateTokens(id).getOrThrow() }
                .onSuccess { tokens ->
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = AuthResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
                    )
                }
                .handleError(call = call, mapToCode = AuthException::toStatusCode)
        }
    }

    private fun Routing.refresh() {
        post<Auth.Refresh> {
            val request = call.receive<RefreshTokenRequest>()
            authRepository
                .refreshAccessToken(request.refreshToken)
                .onSuccess { tokens ->
                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
                    )
                }
                .handleError(call = call, mapToCode = AuthException::toStatusCode)
        }
    }

    private fun Routing.logout() {
        post<Auth.Logout> {
            val request = call.receive<RefreshTokenRequest>()
            authRepository.logout(request.refreshToken)
                .onSuccess { call.respond(HttpStatusCode.OK, "Logged out successfully".toMessageResponse()) }
                .handleError(call = call, mapToCode = AuthException::toStatusCode)
        }
    }
}