package not.djinni.model.token

import kotlinx.datetime.Instant

data class RefreshToken(
    val id: Long,
    val userId: Long,
    val token: String,
    val expiresAt: Instant
)
