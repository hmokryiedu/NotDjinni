package not.djinni.data

import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import not.djinni.domain.repository.AuthRepository
import org.koin.core.annotation.Single
import java.security.MessageDigest

@Single([AuthRepository::class])
class DefaultAuthRepository(private val userDao: UserDao) : AuthRepository {

    private val passwordRegex by lazy { PASSWORD_REGEX.toRegex() }

    override suspend fun login(email: String, password: String) = runCatching<Unit> {
        val user = userDao.getUserByEmail(email) ?: throw IllegalArgumentException("User not found")
        if (user.password != password.hash()) throw IllegalArgumentException("Incorrect password")
    }

    override suspend fun register(email: String, password: String) = runCatching {
        if (userDao.getUserByEmail(email = email) != null) throw IllegalArgumentException("Email already exists")
        if (!passwordRegex.matches(password)) throw IllegalArgumentException("Password does not match requirements")
        userDao.upsertUser(UserEntity(email = email, password = password.hash()))
    }

    private fun String.hash(): String = MessageDigest.getInstance("SHA-256").digest(this.toByteArray()).toHexString()

    private companion object {
        const val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,60}$"""
    }
}