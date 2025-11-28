package not.djinni.database.api.user

data class UserEntity(
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String
)