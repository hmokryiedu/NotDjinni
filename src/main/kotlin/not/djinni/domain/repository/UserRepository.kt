package not.djinni.domain.repository

import not.djinni.model.User

interface UserRepository {

    suspend fun getUser(userId: Long): Result<User>
}