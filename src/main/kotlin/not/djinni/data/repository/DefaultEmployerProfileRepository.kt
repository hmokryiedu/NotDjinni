package not.djinni.data.repository

import not.djinni.data.mapper.toEntity
import not.djinni.data.mapper.toProfileWithCompanyDomain
import not.djinni.database.api.employer.CompanyDao
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.domain.exception.employer.EmployerProfileException
import not.djinni.domain.repository.EmployerProfileRepository
import not.djinni.model.role.EmployerProfile
import org.koin.core.annotation.Single

@Single(binds = [EmployerProfileRepository::class])
class DefaultEmployerProfileRepository(
    private val employerProfileDao: EmployerProfileDao,
    private val companyDao: CompanyDao
) : EmployerProfileRepository {

    override suspend fun getProfile(userId: Long) = runCatching {
        employerProfileDao.getProfileByUserId(userId)
            ?.toProfileWithCompanyDomain()
            ?: throw EmployerProfileException.ProfileNotFound()
    }

    override suspend fun createProfile(userId: Long, profile: EmployerProfile) = runCatching {
        if (employerProfileDao.profileExists(userId)) {
            throw EmployerProfileException.ProfileAlreadyExists()
        }
        if (companyDao.getCompany(profile.companyId) == null) {
            throw EmployerProfileException.CompanyNotFound()
        }
        val profileId = employerProfileDao.createProfile(profile.toEntity(userId))
        employerProfileDao.getProfile(profileId)
            ?.toProfileWithCompanyDomain()
            ?: throw EmployerProfileException.ProfileNotFound()
    }

    override suspend fun updateProfile(userId: Long, role: String) = runCatching<Unit> {
        val existingProfile = employerProfileDao.getProfileByUserId(userId)
            ?: throw EmployerProfileException.ProfileNotFound()

        val updatedProfile = existingProfile.profile.copy(role = role)
        val updated = employerProfileDao.updateProfile(updatedProfile)
        if (!updated) throw EmployerProfileException.ProfileNotFound()
    }

    override suspend fun deleteProfile(userId: Long) = runCatching<Unit> {
        val existingProfile = employerProfileDao.getProfileByUserId(userId)
            ?: throw EmployerProfileException.ProfileNotFound()

        val deleted = employerProfileDao.deleteProfile(existingProfile.profile.id)
        if (!deleted) throw EmployerProfileException.ProfileNotFound()
    }
}
