package not.djinni.presentation.router.routes.user.response

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String,
)