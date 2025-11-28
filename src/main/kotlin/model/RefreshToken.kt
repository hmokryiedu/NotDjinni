package not.djinni.model

import java.time.LocalDateTime

data class RefreshToken(
    val id: Long,
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime
)
