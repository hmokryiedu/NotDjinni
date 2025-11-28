package not.djinni.database.impl.seeker

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Single

@Single([SeekerProfileDao::class])
class DefaultSeekerProfileDao(
    private val workExperienceDao: WorkExperienceDao
) : SeekerProfileDao {

    override suspend fun createProfile(profile: SeekerProfileEntity): Long {
        // Validation
        if (profile.experienceYears < 0) {
            throw IllegalArgumentException("Experience years cannot be negative")
        }
        if (profile.specialty.isBlank()) {
            throw IllegalArgumentException("Specialty cannot be blank")
        }

        return runQuery {
            SeekerProfileTableEntity.new {
                userId = EntityID(profile.userId, UserTable)
                specialty = profile.specialty
                experienceYears = profile.experienceYears
                desiredSalary = profile.desiredSalary
                aboutMe = profile.aboutMe
            }.id.value
        }
    }

    override suspend fun getProfile(id: Long): SeekerProfileEntity? = runQuery {
        SeekerProfileTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity? = runQuery {
        SeekerProfileTableEntity.find { SeekerProfileTable.userId eq EntityID(userId, UserTable) }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun updateProfile(profile: SeekerProfileEntity): Boolean {
        // Validation
        if (profile.experienceYears < 0) {
            throw IllegalArgumentException("Experience years cannot be negative")
        }
        if (profile.specialty.isBlank()) {
            throw IllegalArgumentException("Specialty cannot be blank")
        }

        return runQuery {
            SeekerProfileTableEntity.findById(profile.id)?.apply {
                specialty = profile.specialty
                experienceYears = profile.experienceYears
                desiredSalary = profile.desiredSalary
                aboutMe = profile.aboutMe
            } != null
        }
    }

    override suspend fun profileExists(userId: Long): Boolean = runQuery {
        !SeekerProfileTableEntity.find { SeekerProfileTable.userId eq EntityID(userId, UserTable) }.empty()
    }

    override suspend fun getProfileWithWorkExperience(userId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? {
        val profile = getProfileByUserId(userId) ?: return null
        val workExperiences = workExperienceDao.getWorkExperiencesByProfileId(profile.id)
        return Pair(profile, workExperiences)
    }
}
