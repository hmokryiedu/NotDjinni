package not.djinni.domain.model.error.auth

import not.djinni.domain.model.User

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()

}