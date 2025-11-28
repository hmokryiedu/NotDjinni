package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.auth.TokenProvider
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.token.RefreshTokenDao
import not.djinni.database.api.token.RefreshTokenEntity
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.model.token.AuthTokens
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Single([AuthRepository::class])
class DefaultAuthRepository(
    private val userDao: UserDao,
    private val refreshTokenDao: RefreshTokenDao,
    private val tokenProvider: TokenProvider
) : AuthRepository {

    private val passwordRegex by lazy { PASSWORD_REGEX.toRegex() }

    override suspend fun login(email: String, password: String) = runCatching {
        return@runCatching userDao.getUserByEmail(email)
            ?.also { if (it.password != password.hash()) throw AuthException.InvalidCredentials() }
            ?.id ?: throw AuthException.UserNotFound()
    }

    override suspend fun register(name: String, email: String, password: String) = runCatching {
        validateName(name)
        if (userDao.getUserByEmail(email = email) != null) throw AuthException.EmailAlreadyInUse()
        if (!passwordRegex.matches(password)) throw AuthException.WeakPassword()
        return@runCatching userDao.upsertUser(UserEntity(name = name, email = email, password = password.hash()))
    }

    override suspend fun generateTokens(userId: Long): Result<AuthTokens> = runCatching {
        val accessToken = tokenProvider.generate(userId)
        val refreshToken = UUID.randomUUID().toString()
        val duration = REFRESH_TOKEN_VALIDITY_DAYS.toDuration(DurationUnit.DAYS)
        val expiresAt = Clock.System.now().plus(duration)
        val entity = RefreshTokenEntity(
            userId = userId,
            token = refreshToken,
            expiresAt = expiresAt
        )
        refreshTokenDao.insert(entity)
        AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    override suspend fun refreshAccessToken(refreshToken: String): Result<AuthTokens> = runCatching {
        val storedToken = refreshTokenDao.findByToken(refreshToken) ?: run {
            throw AuthException.InvalidRefreshToken()
        }
        if (storedToken.expiresAt < Clock.System.now()) {
            refreshTokenDao.deleteByToken(refreshToken)
            throw AuthException.RefreshTokenExpired()
        }
        refreshTokenDao.deleteByToken(refreshToken)
        generateTokens(storedToken.userId).getOrThrow()
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        val deleted = refreshTokenDao.deleteByToken(refreshToken)
        if (!deleted) throw AuthException.InvalidRefreshToken()
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