package not.djinni.database.api.user

interface UserDao {

    suspend fun getUser(id: Long): UserEntity?
    suspend fun upsertUser(user: UserEntity): Long
    suspend fun getUserByEmail(email: String): UserEntity?
}
