package not.djinni.presentation.router.routes.application.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HasAppliedResponse(
    @SerialName("has_applied")
    val hasApplied: Boolean
)
