package not.djinni.domain.repository

import not.djinni.domain.model.User

interface AuthRepository {

    suspend fun getUser(id: Long): User?
    suspend fun login(email: String, password: String): Result<Long>
    suspend fun register(email: String, password: String): Result<Long>
}
