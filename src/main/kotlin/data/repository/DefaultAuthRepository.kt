package not.djinni.data.repository

import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import not.djinni.domain.repository.AuthRepository
import not.djinni.domain.exception.auth.AuthException
import org.koin.core.annotation.Single
import java.security.MessageDigest

@Single([AuthRepository::class])
class DefaultAuthRepository(private val userDao: UserDao) : AuthRepository {

    private val passwordRegex by lazy { PASSWORD_REGEX.toRegex() }

    override suspend fun login(email: String, password: String) = runCatching {
        val user = userDao.getUserByEmail(email) ?: throw AuthException.UserNotFound()
        if (user.password != password.hash()) throw AuthException.InvalidCredentials()
        return@runCatching user.id
    }

    override suspend fun register(email: String, password: String) = runCatching {
        if (userDao.getUserByEmail(email = email) != null) throw AuthException.EmailAlreadyInUse()
        if (!passwordRegex.matches(password)) throw AuthException.WeakPassword()
        return@runCatching userDao.upsertUser(UserEntity(email = email, password = password.hash()))
    }

    private fun String.hash(): String = MessageDigest.getInstance("SHA-256").digest(this.toByteArray()).toHexString()

    private companion object {
        const val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,60}$"""
    }
}