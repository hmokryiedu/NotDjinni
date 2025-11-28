package not.djinni.data.mapper

import not.djinni.database.api.user.UserEntity
import not.djinni.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
    )
}