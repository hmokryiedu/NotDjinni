package not.djinni.presentation.router.routes.common.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    @SerialName("message")
    val message: String
)

fun String.toErrorResponse(): ErrorResponse = ErrorResponse(this)