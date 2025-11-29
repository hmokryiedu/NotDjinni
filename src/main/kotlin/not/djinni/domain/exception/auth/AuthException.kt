package not.djinni.domain.exception.auth

sealed class AuthException(override val message: String) : Throwable() {
    class UserNotFound : AuthException("User not found")
    class InvalidCredentials : AuthException("Invalid credentials")
    class EmailAlreadyInUse : AuthException("Email already in use")
    class WeakPassword : AuthException("Weak password")
    data class InvalidName(override val message: String) : AuthException(message)
    data class InvalidAccessToken(override val message: String = "Invalid access token") : AuthException(message)
    data class InvalidRefreshToken(override val message: String = "Invalid refresh token") : AuthException(message)
    data class RefreshTokenExpired(override val message: String = "Refresh token expired") : AuthException(message)
}
