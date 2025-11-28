package not.djinni.data.mapper

import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import not.djinni.model.RefreshToken

fun RefreshTokenEntity.toDomain(): RefreshToken {
    return RefreshToken(
        id = id,
        userId = userId,
        token = token,
        expiresAt = expiresAt
    )
}

fun RefreshToken.toEntity(): RefreshTokenEntity {
    return RefreshTokenEntity(
        id = id,
        userId = userId,
        token = token,
        expiresAt = expiresAt
    )
}
