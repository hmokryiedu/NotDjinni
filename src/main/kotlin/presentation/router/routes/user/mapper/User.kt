package not.djinni.presentation.router.routes.user.mapper

import not.djinni.model.User
import not.djinni.presentation.router.common.response.user.UserResponse

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = id,
        name = name,
        email = email,
    )
}