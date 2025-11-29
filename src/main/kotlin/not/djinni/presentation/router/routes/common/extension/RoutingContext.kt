package not.djinni.presentation.router.routes.common.extension

import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import not.djinni.domain.exception.auth.AuthException
import not.djinni.presentation.router.routes.auth.mapper.toStatusCode
import not.djinni.presentation.router.routes.common.auth.JwtAuth.USER_ID_CLAIM_NAME
import not.djinni.presentation.router.routes.common.mapper.toErrorResult

suspend fun RoutingContext.getUserIdFromTokenOrSendError(): Long? {
    return getClaim<Long>(USER_ID_CLAIM_NAME) ?: run {
        val exception = AuthException.InvalidAccessToken()
        val errorResult = exception.toErrorResult(AuthException::toStatusCode)
        call.respond(status = errorResult.code, message = errorResult.model)
        null
    }
}