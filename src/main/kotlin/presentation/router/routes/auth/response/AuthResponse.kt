package not.djinni.presentation.router.routes.auth.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.common.response.user.UserResponse

@Serializable
data class AuthResponse(
    @SerialName("user")
    val user: UserResponse,
    @SerialName("token")
    val token: String
)