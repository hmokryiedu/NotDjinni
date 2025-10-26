package not.djinni.domain.repository

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<Long>
    suspend fun register(email: String, password: String): Result<Long>
}
