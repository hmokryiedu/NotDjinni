package not.djinni.database.api.employer

interface EmployerProfileDao {
    suspend fun createProfile(profile: EmployerProfileEntity): Long
    suspend fun getProfile(id: Long): EmployerProfileWithCompany?
    suspend fun getProfileByUserId(userId: Long): EmployerProfileWithCompany?
    suspend fun updateProfile(profile: EmployerProfileEntity): Boolean
    suspend fun deleteProfile(id: Long): Boolean
    suspend fun profileExists(userId: Long): Boolean
}

data class EmployerProfileWithCompany(
    val profile: EmployerProfileEntity,
    val company: CompanyEntity
)
