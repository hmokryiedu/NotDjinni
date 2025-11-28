package not.djinni.domain.repository

import not.djinni.model.User

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>

    suspend fun generateTokens(userId: Long): Result<AuthTokens>
    suspend fun refreshAccessToken(refreshToken: String): Result<AuthTokens>
    suspend fun logout(refreshToken: String): Result<Unit>
    suspend fun logoutAll(userId: Long): Result<Unit>
}
