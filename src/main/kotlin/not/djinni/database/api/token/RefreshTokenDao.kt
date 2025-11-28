package not.djinni.database.api.token

interface RefreshTokenDao {

    suspend fun findByToken(token: String): RefreshTokenEntity?

    suspend fun insert(entity: RefreshTokenEntity): Long

    suspend fun deleteByToken(token: String): Boolean

    suspend fun deleteAllForUser(userId: Long): Int

    suspend fun deleteExpired(): Int
}
