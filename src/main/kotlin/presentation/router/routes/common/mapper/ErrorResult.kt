package not.djinni.presentation.router.routes.common.mapper

import io.ktor.http.*
import not.djinni.presentation.router.routes.common.model.ErrorResult
import not.djinni.presentation.router.routes.common.response.toErrorResponse

inline fun <reified T : Any> Throwable.toErrorResult(
    mapToCode: T.() -> HttpStatusCode?
): ErrorResult {
    return ErrorResult(
        code = (this as? T)?.mapToCode() ?: ErrorResult.Default.code,
        model = message?.toErrorResponse() ?: ErrorResult.Default.model
    )
}