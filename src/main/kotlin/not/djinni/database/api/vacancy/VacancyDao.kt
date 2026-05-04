package not.djinni.database.api.vacancy

import not.djinni.database.api.employer.CompanyEntity

interface VacancyDao {
    suspend fun createVacancy(vacancy: VacancyEntity): Long
    suspend fun getVacancy(id: Long): VacancyEntity?
    suspend fun getVacancyWithDetails(id: Long): VacancyWithDetailsEntity?
    suspend fun updateVacancy(vacancy: VacancyEntity): Boolean
    suspend fun deleteVacancy(id: Long): Boolean
    suspend fun getVacancies(filter: VacancyFilter, limit: Int = 20, offset: Int = 0): List<VacancyEntity>
    suspend fun getVacanciesWithDetails(filter: VacancyFilter, limit: Int = 20, offset: Int = 0): List<VacancyWithDetailsEntity>
    suspend fun countVacancies(filter: VacancyFilter): Int
    suspend fun getVacanciesByCompany(companyId: Long, limit: Int = 20, offset: Int = 0): List<VacancyWithDetailsEntity>
    suspend fun getRecentVacancies(limit: Int = 10): List<VacancyEntity>
    suspend fun updateVacancyStatus(id: Long, status: not.djinni.model.vacancy.VacancyStatusCode): Boolean
    suspend fun vacancyExists(id: Long): Boolean
}

data class VacancyWithDetailsEntity(
    val vacancy: VacancyEntity,
    val company: CompanyEntity,
    val applicationsCount: Int = vacancy.applicationsCount
)
