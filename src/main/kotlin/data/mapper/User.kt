package not.djinni.data.mapper

import not.djinni.database.api.user.UserEntity
import not.djinni.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        email = email,
        password = password,
    )
}