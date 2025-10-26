package not.djinni.presentation.router.routes.user.mapper

import io.ktor.http.*
import not.djinni.domain.exception.user.UserException

fun UserException.toStatusCode(): HttpStatusCode = when (this) {
    is UserException.NotFound -> HttpStatusCode.NotFound
}