package not.djinni.database.api.refreshtoken

interface RefreshTokenDao {

    /**
     * Find refresh token by token string
     * @return RefreshTokenEntity or null if not found
     */
    suspend fun findByToken(token: String): RefreshTokenEntity?

    /**
     * Insert new refresh token
     * @return generated token ID
     */
    suspend fun insert(entity: RefreshTokenEntity): Long

    /**
     * Delete refresh token by token string
     * @return true if deleted, false if not found
     */
    suspend fun deleteByToken(token: String): Boolean

    /**
     * Delete all refresh tokens for a user
     * @return number of tokens deleted
     */
    suspend fun deleteAllForUser(userId: Long): Int

    /**
     * Delete expired tokens (cleanup)
     * @return number of tokens deleted
     */
    suspend fun deleteExpired(): Int
}
