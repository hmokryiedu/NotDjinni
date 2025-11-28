package not.djinni.presentation.router.common.response.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MessageResponse(
    @SerialName("message")
    val message: String
)

internal fun String.toMessageResponse() = MessageResponse(message = this)