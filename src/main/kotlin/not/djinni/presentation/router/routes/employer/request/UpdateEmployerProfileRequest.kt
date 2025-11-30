package not.djinni.presentation.router.routes.employer.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateEmployerProfileRequest(
    @SerialName("role")
    val role: String
)
