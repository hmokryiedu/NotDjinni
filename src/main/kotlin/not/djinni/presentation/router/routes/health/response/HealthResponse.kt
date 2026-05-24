package not.djinni.presentation.router.routes.health.response

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String
)
