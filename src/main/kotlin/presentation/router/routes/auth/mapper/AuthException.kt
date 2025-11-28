package not.djinni.presentation.router.routes.auth.mapper

import io.ktor.http.*
import not.djinni.domain.exception.auth.AuthException

fun AuthException.toStatusCode(): HttpStatusCode = when (this) {
    is AuthException.EmailAlreadyInUse -> HttpStatusCode.Conflict
    is AuthException.InvalidCredentials -> HttpStatusCode.Unauthorized
    is AuthException.UserNotFound -> HttpStatusCode.NotFound
    is AuthException.WeakPassword -> HttpStatusCode.UnprocessableEntity
    is AuthException.InvalidName -> HttpStatusCode.BadRequest
    is AuthException.InvalidRefreshToken -> HttpStatusCode.Unauthorized
    is AuthException.RefreshTokenExpired -> HttpStatusCode.Unauthorized
}