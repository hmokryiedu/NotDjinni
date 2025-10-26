package not.djinni.domain.exception.auth

sealed class AuthException(override val message: String) : Throwable() {
    class UserNotFound : AuthException("User not found")
    class InvalidCredentials : AuthException("Invalid credentials")
    class EmailAlreadyInUse : AuthException("Email already in use")
    class WeakPassword : AuthException("Weak password")
}
