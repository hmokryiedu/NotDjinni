package not.djinni.presentation.router.routes.seeker.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkExperienceIdResponse(
    @SerialName("id")
    val id: Long
)