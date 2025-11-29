package not.djinni.presentation.router.routes.common.model

import io.ktor.http.HttpStatusCode
import not.djinni.presentation.router.routes.common.response.ErrorResponse

data class ErrorResult(
    val code: HttpStatusCode,
    val model: ErrorResponse
) {
    companion object {
        val Default = ErrorResult(
            code = HttpStatusCode.InternalServerError,
            model = ErrorResponse(message = "An unexpected error occurred.")
        )
    }
}