package not.djinni.database.api.refreshtoken

import java.time.LocalDateTime

data class RefreshTokenEntity(
    val id: Long = 0,
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime
)
