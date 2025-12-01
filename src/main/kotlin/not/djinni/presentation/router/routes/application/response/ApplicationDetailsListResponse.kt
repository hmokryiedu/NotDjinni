package not.djinni.presentation.router.routes.application.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationDetailsListResponse(
    @SerialName("applications")
    val applications: List<ApplicationDetailsResponse>,
)
