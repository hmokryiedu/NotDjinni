package not.djinni.presentation.router.routes.application.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateApplicationStatusRequest(
    @SerialName("status")
    val status: ApplicationStatusRequest
)
