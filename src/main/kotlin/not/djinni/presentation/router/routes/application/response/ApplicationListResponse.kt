package not.djinni.presentation.router.routes.application.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationListResponse(
    @SerialName("applications")
    val applications: List<ApplicationResponse>,
)
