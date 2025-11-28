package not.djinni.domain.repository

import not.djinni.model.User

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
}
