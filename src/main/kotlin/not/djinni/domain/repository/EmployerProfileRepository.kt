package not.djinni.domain.repository

import not.djinni.model.role.EmployerProfile
import not.djinni.model.role.EmployerProfileWithCompany

interface EmployerProfileRepository {
    suspend fun getProfile(userId: Long): Result<EmployerProfileWithCompany>
    suspend fun createProfile(userId: Long, profile: EmployerProfile): Result<EmployerProfileWithCompany>
    suspend fun updateProfile(userId: Long, role: String): Result<Unit>
    suspend fun deleteProfile(userId: Long): Result<Unit>
}
