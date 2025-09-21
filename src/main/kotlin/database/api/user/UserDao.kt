package not.djinni.database.api.user

interface UserDao {

    suspend fun upsertUser(user: UserEntity)
    suspend fun getUserByEmail(email: String): UserEntity?
}
