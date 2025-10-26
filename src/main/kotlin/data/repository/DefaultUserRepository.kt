package not.djinni.data.repository

import not.djinni.data.mapper.toDomain
import not.djinni.database.api.user.UserDao
import not.djinni.domain.repository.UserRepository
import not.djinni.domain.exception.user.UserException
import org.koin.core.annotation.Single

@Single(binds = [UserRepository::class])
class DefaultUserRepository(
    private val userDao: UserDao,
) : UserRepository {

    override suspend fun getUser(userId: Long) = runCatching {
        userDao.getUser(userId)?.toDomain() ?: run {
            throw UserException.NotFound()
        }
    }
}