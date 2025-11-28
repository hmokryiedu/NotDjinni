package not.djinni.presentation.router.extension

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.presentation.router.routes.common.mapper.toErrorResult

suspend inline fun <reified T, reified R> Result<T>.handleError(
    call: RoutingCall,
    mapToCode: R.() -> HttpStatusCode?
) = onFailure {
    val errorResult = it.toErrorResult(mapToCode)
    call.respond(status = errorResult.code, message = errorResult.model)
}