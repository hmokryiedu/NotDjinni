package not.djinni.presentation.router.routes.application.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateApplicationRequest(
    @SerialName("cover_letter")
    val coverLetter: String? = null
)
