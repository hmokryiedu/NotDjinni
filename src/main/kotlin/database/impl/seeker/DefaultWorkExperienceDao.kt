package not.djinni.database.impl.seeker

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.seeker.WorkExperienceDao
import not.djinni.database.api.seeker.WorkExperienceEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.koin.core.annotation.Single

@Single([WorkExperienceDao::class])
class DefaultWorkExperienceDao : WorkExperienceDao {

    override suspend fun createWorkExperience(experience: WorkExperienceEntity): Long {
        // Validation
        validateWorkExperience(experience)

        return runQuery {
            WorkExperienceTableEntity.new {
                profileId = EntityID(experience.profileId, SeekerProfileTable)
                companyName = experience.companyName
                position = experience.position
                description = experience.description
                startDate = experience.startDate
                endDate = experience.endDate
            }.id.value
        }
    }

    override suspend fun getWorkExperience(id: Long): WorkExperienceEntity? = runQuery {
        WorkExperienceTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getWorkExperiencesByProfileId(profileId: Long): List<WorkExperienceEntity> = runQuery {
        WorkExperienceTableEntity.find {
            WorkExperienceTable.profileId eq EntityID(profileId, SeekerProfileTable)
        }
            .orderBy(WorkExperienceTable.startDate to SortOrder.DESC)
            .map { it.toEntity() }
    }

    override suspend fun updateWorkExperience(experience: WorkExperienceEntity): Boolean {
        // Validation
        validateWorkExperience(experience)

        return runQuery {
            WorkExperienceTableEntity.findById(experience.id)?.apply {
                companyName = experience.companyName
                position = experience.position
                description = experience.description
                startDate = experience.startDate
                endDate = experience.endDate
            } != null
        }
    }

    override suspend fun deleteWorkExperience(id: Long): Boolean = runQuery {
        WorkExperienceTableEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    private fun validateWorkExperience(experience: WorkExperienceEntity) {
        if (experience.companyName.isBlank()) {
            throw IllegalArgumentException("Company name cannot be blank")
        }
        if (experience.position.isBlank()) {
            throw IllegalArgumentException("Position cannot be blank")
        }
        if (experience.endDate != null && experience.startDate > experience.endDate) {
            throw IllegalArgumentException("Start date cannot be after end date")
        }
        if (experience.startDate.isAfter(java.time.LocalDate.now())) {
            throw IllegalArgumentException("Start date cannot be in the future")
        }
    }
}
