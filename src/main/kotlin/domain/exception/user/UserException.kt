package not.djinni.domain.exception.user

sealed class UserException(override val message: String) : Throwable() {
    class NotFound : UserException("User not found")
}
