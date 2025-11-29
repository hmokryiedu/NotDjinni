package not.djinni.database.impl.seeker

import kotlinx.datetime.Clock
import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.seeker.WorkExperienceDao
import not.djinni.database.api.seeker.WorkExperienceEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.koin.core.annotation.Single

@Single([WorkExperienceDao::class])
class DefaultWorkExperienceDao : WorkExperienceDao {

    override suspend fun createWorkExperience(experience: WorkExperienceEntity) = runQuery {
        validateWorkExperience(experience)
        fillWorkExperienceEntity(experience = experience)
    }

    override suspend fun createWorkExperiences(experiences: List<WorkExperienceEntity>): List<Long> = runQuery {
        experiences.map { experience ->
            validateWorkExperience(experience)
            fillWorkExperienceEntity(experience = experience)
        }
    }

    override suspend fun getWorkExperience(id: Long): WorkExperienceEntity? = runQuery {
        WorkExperienceTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getWorkExperiencesByProfileId(profileId: Long): List<WorkExperienceEntity> = runQuery {
        WorkExperienceTableEntity.find { WorkExperienceTable.profileId eq EntityID(profileId, SeekerProfileTable) }
            .orderBy(Pair(WorkExperienceTable.startDate, SortOrder.DESC))
            .map { it.toEntity() }
    }

    override suspend fun updateWorkExperience(experience: WorkExperienceEntity): Boolean {
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
        WorkExperienceTableEntity.findById(id)?.also(WorkExperienceTableEntity::delete) != null
    }

    override suspend fun deleteAllWorkExperiencesByProfileId(profileId: Long) = runQuery {
        WorkExperienceTableEntity
            .find { WorkExperienceTable.profileId eq EntityID(profileId, SeekerProfileTable) }
            .forEach { it.delete() }
    }

    private fun fillWorkExperienceEntity(experience: WorkExperienceEntity): Long {
        return WorkExperienceTableEntity.new {
            profileId = EntityID(experience.profileId, SeekerProfileTable)
            companyName = experience.companyName
            position = experience.position
            description = experience.description
            startDate = experience.startDate
            endDate = experience.endDate
        }.id.value
    }

    private fun validateWorkExperience(experience: WorkExperienceEntity) {
        when {
            experience.companyName.isBlank() -> error("Company name cannot be blank")
            experience.position.isBlank() -> error("Position cannot be blank")
            experience.endDate != null && experience.startDate > experience.endDate -> error("Start date cannot be after end date")
            experience.startDate >= Clock.System.now() -> error("Start date cannot be in the future")
        }
    }
}
