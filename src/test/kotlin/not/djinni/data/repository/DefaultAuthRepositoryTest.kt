package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import not.djinni.auth.TokenProvider
import not.djinni.auth.model.JwtConfiguration
import not.djinni.database.api.token.RefreshTokenDao
import not.djinni.database.api.token.RefreshTokenEntity
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAuthRepositoryTest {

    @Test
    fun `generateTokens stores refresh token that fits db length limit`() = runBlocking {
        val refreshTokenDao = LengthLimitedRefreshTokenDao(maxLength = MAX_REFRESH_TOKEN_LENGTH)
        val repository = DefaultAuthRepository(
            userDao = FakeUserDao(),
            tokenProvider = SequenceTokenProvider(
                tokens = listOf(
                    ACCESS_TOKEN,
                    "r".repeat(MAX_REFRESH_TOKEN_LENGTH + 2),
                )
            ),
            refreshTokenDao = refreshTokenDao,
        )

        val result = repository.generateTokens(USER_ID)

        assertTrue(result.isSuccess)
        val tokens = result.getOrThrow()
        assertEquals(ACCESS_TOKEN, tokens.accessToken)
        assertTrue(tokens.refreshToken.length <= MAX_REFRESH_TOKEN_LENGTH)
        assertEquals(tokens.refreshToken, refreshTokenDao.inserted.single().token)
    }

    private class FakeUserDao : UserDao {
        override suspend fun getUser(id: Long): UserEntity? = null
        override suspend fun upsertUser(user: UserEntity): Long = user.id ?: USER_ID
        override suspend fun getUserByEmail(email: String): UserEntity? = null
    }

    private class SequenceTokenProvider(
        private val tokens: List<String>,
    ) : TokenProvider {
        override val configuration: JwtConfiguration
            get() = throw UnsupportedOperationException("Not needed in repository tests")

        private var index = 0

        override fun initialize() = Unit

        override fun generate(id: Long): String {
            val value = tokens.getOrElse(index) { tokens.last() }
            index += 1
            return value
        }
    }

    private class LengthLimitedRefreshTokenDao(
        private val maxLength: Int,
    ) : RefreshTokenDao {
        val inserted = mutableListOf<RefreshTokenEntity>()

        override suspend fun findByToken(token: String): RefreshTokenEntity? = inserted.find { it.token == token }

        override suspend fun insert(entity: RefreshTokenEntity): Long {
            require(entity.token.length <= maxLength) {
                "Refresh token length ${entity.token.length} exceeds limit $maxLength"
            }
            inserted += entity
            return INSERTED_TOKEN_ID
        }

        override suspend fun deleteByToken(token: String): Boolean = false
        override suspend fun deleteAllForUser(userId: Long): Int = 0
        override suspend fun deleteExpired(): Int = 0
    }

    private companion object {
        const val USER_ID = 42L
        const val ACCESS_TOKEN = "access.jwt.token"
        const val MAX_REFRESH_TOKEN_LENGTH = 512
        const val INSERTED_TOKEN_ID = 1L
    }
}
