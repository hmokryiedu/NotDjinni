package not.djinni.data.repository

import not.djinni.auth.TokenProvider
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.refreshtoken.RefreshTokenDao
import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import not.djinni.domain.repository.AuthRepository
import not.djinni.domain.repository.AuthTokens
import not.djinni.domain.exception.auth.AuthException
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Single([AuthRepository::class])
class DefaultAuthRepository(
    private val userDao: UserDao,
    private val refreshTokenDao: RefreshTokenDao,
    private val tokenProvider: TokenProvider
) : AuthRepository {

    private val passwordRegex by lazy { PASSWORD_REGEX.toRegex() }

    override suspend fun login(email: String, password: String) = runCatching {
        val user = userDao.getUserByEmail(email) ?: throw AuthException.UserNotFound()
        if (user.password != password.hash()) throw AuthException.InvalidCredentials()
        return@runCatching user.toDomain()
    }

    override suspend fun register(name: String, email: String, password: String) = runCatching {
        validateName(name)
        if (userDao.getUserByEmail(email = email) != null) throw AuthException.EmailAlreadyInUse()
        if (!passwordRegex.matches(password)) throw AuthException.WeakPassword()
        return@runCatching UserEntity(name = name, email = email, password = password.hash())
            .also { userDao.upsertUser(it) }
            .toDomain()
    }

    override suspend fun generateTokens(userId: Long): Result<AuthTokens> = runCatching {
        // Generate short-lived access token (30 minutes)
        val accessToken = tokenProvider.generate(userId)

        // Generate long-lived refresh token (30 days)
        val refreshToken = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)

        // Store refresh token in database
        refreshTokenDao.insert(
            RefreshTokenEntity(
                userId = userId,
                token = refreshToken,
                expiresAt = expiresAt
            )
        )

        AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    override suspend fun refreshAccessToken(refreshToken: String): Result<AuthTokens> = runCatching {
        // Find token in database
        val storedToken = refreshTokenDao.findByToken(refreshToken)
            ?: throw AuthException.InvalidRefreshToken()

        // Check if expired
        if (storedToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenDao.deleteByToken(refreshToken)
            throw AuthException.RefreshTokenExpired()
        }

        // Delete old refresh token (rotation for security)
        refreshTokenDao.deleteByToken(refreshToken)

        // Generate new token pair
        generateTokens(storedToken.userId).getOrThrow()
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        val deleted = refreshTokenDao.deleteByToken(refreshToken)
        if (!deleted) throw AuthException.InvalidRefreshToken()
    }

    override suspend fun logoutAll(userId: Long): Result<Unit> = runCatching {
        refreshTokenDao.deleteAllForUser(userId)
    }

    private fun validateName(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) throw AuthException.InvalidName("Name cannot be blank")
        if (trimmedName.length < MIN_NAME_LENGTH) throw AuthException.InvalidName("Name must be at least $MIN_NAME_LENGTH characters")
        if (trimmedName.length > MAX_NAME_LENGTH) throw AuthException.InvalidName("Name must be at most $MAX_NAME_LENGTH characters")
    }

    private fun String.hash(): String = MessageDigest.getInstance("SHA-256").digest(this.toByteArray()).toHexString()

    private companion object {
        const val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,60}$"""
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 100
        const val REFRESH_TOKEN_VALIDITY_DAYS = 30L
    }
}