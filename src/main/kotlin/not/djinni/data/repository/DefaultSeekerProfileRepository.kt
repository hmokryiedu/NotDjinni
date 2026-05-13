package not.djinni.data.repository

import not.djinni.data.mapper.toDomain
import not.djinni.data.mapper.toEntity
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.WorkExperienceDao
import not.djinni.domain.exception.seeker.SeekerProfileException
import not.djinni.domain.repository.SeekerProfileRepository
import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience
import org.koin.core.annotation.Single

@Single(binds = [SeekerProfileRepository::class])
class DefaultSeekerProfileRepository(
    private val seekerProfileDao: SeekerProfileDao,
    private val workExperienceDao: WorkExperienceDao
) : SeekerProfileRepository {

    override suspend fun getProfile(userId: Long) = runSeekerProfileCatching {
        seekerProfileDao.getProfileWithWorkExperienceByUserId(userId)?.toDomain() ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
    }

    override suspend fun createProfile(userId: Long, profile: SeekerProfile) = runSeekerProfileCatching {
        if (seekerProfileDao.profileExists(userId)) throw SeekerProfileException.ProfileAlreadyExists()
        val profileId = seekerProfileDao.createProfile(profile.toEntity(userId)).also { profileId ->
            val experiences = profile.workExperience.map { it.toEntity(profileId) }
            workExperienceDao.createWorkExperiences(experiences)
        }
        getProfileByProfileId(profileId)
    }

    override suspend fun updateProfile(userId: Long, profile: SeekerProfile) = runSeekerProfileCatching {
        val existingProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
        val profileEntity = profile.toEntity(userId).copy(id = existingProfile.id)
        val updated = seekerProfileDao.updateProfile(profileEntity)
        if (!updated) throw SeekerProfileException.ProfileNotFound()
        getProfileByProfileId(existingProfile.id)
    }

    override suspend fun deleteProfile(userId: Long) = runSeekerProfileCatching {
        val existingProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
        workExperienceDao.deleteAllWorkExperiencesByProfileId(existingProfile.id)
        val deleted = seekerProfileDao.deleteProfile(existingProfile.id)
        if (!deleted) throw SeekerProfileException.ProfileNotFound()
    }

    override suspend fun addWorkExperience(userId: Long, experience: WorkExperience) = runSeekerProfileCatching {
        val profile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
        val workExpEntity = experience.toEntity(profile.id)
        workExperienceDao.createWorkExperience(workExpEntity)
        getProfileByProfileId(profile.id)
    }

    override suspend fun updateWorkExperience(
        userId: Long,
        experienceId: Long,
        experience: WorkExperience
    ) = runSeekerProfileCatching {
        val profile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
        workExperienceDao.getWorkExperience(experienceId)
            ?.also { if (it.profileId != profile.id) throw SeekerProfileException.Unauthorized() }
            ?: run { throw SeekerProfileException.WorkExperienceNotFound() }
        val experienceEntity = experience.toEntity(profile.id).copy(id = experienceId)
        val updated = workExperienceDao.updateWorkExperience(experienceEntity)
        if (!updated) throw SeekerProfileException.WorkExperienceNotFound()
        getProfileByProfileId(profile.id)
    }

    override suspend fun deleteWorkExperience(userId: Long, experienceId: Long) = runSeekerProfileCatching {
        val profile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
        workExperienceDao.getWorkExperience(experienceId)
            ?.also { if (it.profileId != profile.id) throw SeekerProfileException.Unauthorized() }
            ?: run { throw SeekerProfileException.WorkExperienceNotFound() }
        val deleted = workExperienceDao.deleteWorkExperience(experienceId)
        if (!deleted) throw SeekerProfileException.WorkExperienceNotFound()
        getProfileByProfileId(profile.id)
    }

    private suspend fun getProfileByProfileId(profileId: Long): SeekerProfile {
        return seekerProfileDao.getProfileWithWorkExperienceByProfileId(profileId)?.toDomain() ?: run {
            throw SeekerProfileException.ProfileNotFound()
        }
    }

    private suspend fun <T> runSeekerProfileCatching(block: suspend () -> T): Result<T> {
        return runCatching { block() }.recoverCatching { error ->
            if (error is IllegalStateException) {
                throw SeekerProfileException.InvalidProfileData(error.message.orEmpty())
            }
            throw error
        }
    }
}
