package not.djinni.database.api.application

import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.model.application.ApplicationStatusCode

interface ApplicationDao {
    suspend fun createApplication(application: ApplicationEntity): Long
    suspend fun getApplication(id: Long): ApplicationWithDetailsEntity?
    suspend fun updateApplication(application: ApplicationEntity): Boolean
    suspend fun updateApplicationStatus(id: Long, statusCode: ApplicationStatusCode): Boolean
    suspend fun deleteApplication(id: Long): Boolean
    suspend fun getApplications(
        filter: ApplicationFilter,
        limit: Int = 20,
        offset: Int = 0
    ): List<ApplicationWithDetailsEntity>
    suspend fun countApplications(filter: ApplicationFilter): Int
    suspend fun hasApplied(vacancyId: Long, jobSeekerId: Long): Boolean
}

data class ApplicationWithDetailsEntity(
    val application: ApplicationEntity,
    val vacancy: VacancyWithDetailsEntity,
    val jobSeeker: SeekerProfileEntity
)
