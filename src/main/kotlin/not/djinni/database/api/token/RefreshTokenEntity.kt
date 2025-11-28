package not.djinni.database.api.token

import kotlinx.datetime.Instant

data class RefreshTokenEntity(
    val id: Long = 0,
    val userId: Long,
    val token: String,
    val expiresAt: Instant
)
