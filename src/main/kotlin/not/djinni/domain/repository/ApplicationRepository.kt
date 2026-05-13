package not.djinni.domain.repository

import not.djinni.database.api.application.ApplicationFilter
import not.djinni.model.application.Application
import not.djinni.model.application.ApplicationStatusCode
import not.djinni.model.application.ApplicationWithDetails

interface ApplicationRepository {
    suspend fun createApplication(userId: Long, application: Application): Result<Application>
    suspend fun getApplication(userId: Long, id: Long): Result<ApplicationWithDetails>
    suspend fun updateApplication(userId: Long, application: Application): Result<Unit>
    suspend fun deleteApplication(userId: Long, id: Long): Result<Unit>
    suspend fun withdrawApplication(userId: Long, id: Long): Result<Unit>
    suspend fun updateApplicationStatus(userId: Long, id: Long, statusCode: ApplicationStatusCode): Result<Unit>
    suspend fun getMyApplications(userId: Long, filter: ApplicationFilter, limit: Int = 20, offset: Int = 0): Result<List<ApplicationWithDetails>>
    suspend fun getVacancyApplications(userId: Long, vacancyId: Long, limit: Int = 20, offset: Int = 0): Result<List<ApplicationWithDetails>>
    suspend fun hasApplied(userId: Long, vacancyId: Long): Result<Boolean>
}
