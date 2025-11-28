package not.djinni.presentation.router.routes.auth

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.auth.mapper.toStatusCode
import not.djinni.presentation.router.routes.auth.request.LoginRequest
import not.djinni.presentation.router.routes.auth.request.RefreshTokenRequest
import not.djinni.presentation.router.routes.auth.request.RegisterRequest
import not.djinni.presentation.router.routes.auth.resources.Auth
import not.djinni.presentation.router.routes.auth.response.AuthResponse
import not.djinni.presentation.router.routes.common.mapper.toErrorResult
import not.djinni.presentation.router.routes.user.mapper.toResponse
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
        logoutAll()
    }

    private fun Routing.login() {
        post<Auth.Login> {
            val loginRequest = call.receive<LoginRequest>()
            authRepository
                .login(loginRequest.email, loginRequest.password)
                .onSuccess { user ->
                    authRepository.generateTokens(user.id)
                        .onSuccess { tokens ->
                            call.respond(
                                HttpStatusCode.OK,
                                AuthResponse(
                                    user = user.toResponse(),
                                    accessToken = tokens.accessToken,
                                    refreshToken = tokens.refreshToken
                                )
                            )
                        }
                        .onFailure { handleError(it) }
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.register() {
        post<Auth.Register> {
            val registerRequest = call.receive<RegisterRequest>()
            authRepository
                .register(registerRequest.name, registerRequest.email, registerRequest.password)
                .onSuccess { user ->
                    authRepository.generateTokens(user.id)
                        .onSuccess { tokens ->
                            call.respond(
                                HttpStatusCode.OK,
                                AuthResponse(
                                    user = user.toResponse(),
                                    accessToken = tokens.accessToken,
                                    refreshToken = tokens.refreshToken
                                )
                            )
                        }
                        .onFailure { handleError(it) }
                }
                .onFailure { handleError(it) }
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
                        mapOf(
                            "access_token" to tokens.accessToken,
                            "refresh_token" to tokens.refreshToken
                        )
                    )
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.logout() {
        post<Auth.Logout> {
            val request = call.receive<RefreshTokenRequest>()
            authRepository
                .logout(request.refreshToken)
                .onSuccess {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.logoutAll() {
        authenticate("auth-jwt") {
            post<Auth.LogoutAll> {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw AuthException.InvalidCredentials()

                authRepository
                    .logoutAll(userId)
                    .onSuccess {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out from all devices"))
                    }
                    .onFailure { handleError(it) }
            }
        }
    }

    private suspend fun handleError(error: Throwable) {
        val errorResult = error.toErrorResult(AuthException::toStatusCode)
        call.respond(status = errorResult.code, message = errorResult.model)
    }
}