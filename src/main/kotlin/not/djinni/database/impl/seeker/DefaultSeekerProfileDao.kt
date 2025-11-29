package not.djinni.database.impl.seeker

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.koin.core.annotation.Single

@Single([SeekerProfileDao::class])
class DefaultSeekerProfileDao : SeekerProfileDao {

    override suspend fun createProfile(profile: SeekerProfileEntity): Long {
        validateProfile(profile)
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
        validateProfile(profile)
        return runQuery {
            SeekerProfileTableEntity.findById(profile.id)?.apply {
                specialty = profile.specialty
                experienceYears = profile.experienceYears
                desiredSalary = profile.desiredSalary
                aboutMe = profile.aboutMe
            } != null
        }
    }

    override suspend fun deleteProfile(id: Long): Boolean = runQuery {
        SeekerProfileTableEntity.findById(id)?.also(SeekerProfileTableEntity::delete) != null
    }

    override suspend fun profileExists(userId: Long): Boolean = runQuery {
        !SeekerProfileTableEntity.find { SeekerProfileTable.userId eq EntityID(userId, UserTable) }.empty()
    }

    override suspend fun getProfileWithWorkExperienceByUserId(userId: Long) = runQuery {
        val profileEntity = SeekerProfileTableEntity
            .find { SeekerProfileTable.userId eq EntityID(userId, UserTable) }
            .firstOrNull() ?: return@runQuery null
        val workExperiences = profileEntity.workExperiences
            .orderBy(WorkExperienceTable.startDate to SortOrder.DESC)
            .map { it.toEntity() }
        Pair(profileEntity.toEntity(), workExperiences)
    }

    override suspend fun getProfileWithWorkExperienceByProfileId(profileId: Long) = runQuery {
        val profileEntity = SeekerProfileTableEntity
            .find { SeekerProfileTable.id eq profileId }
            .firstOrNull() ?: return@runQuery null
        val workExperiences = profileEntity.workExperiences
            .orderBy(WorkExperienceTable.startDate to SortOrder.DESC)
            .map { it.toEntity() }
        Pair(profileEntity.toEntity(), workExperiences)
    }

    private fun validateProfile(profile: SeekerProfileEntity) {
        when {
            profile.experienceYears < 0 -> error("Experience years cannot be negative")
            profile.specialty.isBlank() -> error("Specialty cannot be blank")
        }
    }
}
