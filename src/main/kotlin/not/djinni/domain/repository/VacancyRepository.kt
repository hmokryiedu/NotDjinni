package not.djinni.domain.repository

import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.model.vacancy.Vacancy
import not.djinni.model.vacancy.VacancyStatusCode
import not.djinni.model.vacancy.VacancyWithDetails

interface VacancyRepository {
    suspend fun createVacancy(userId: Long, vacancy: Vacancy): Result<Vacancy>
    suspend fun getVacancy(id: Long): Result<Vacancy>
    suspend fun getVacancyWithDetails(id: Long): Result<VacancyWithDetails>
    suspend fun updateVacancy(userId: Long, vacancy: Vacancy): Result<Unit>
    suspend fun deleteVacancy(userId: Long, id: Long): Result<Unit>
    suspend fun getVacancies(filter: VacancyFilter, limit: Int = 20, offset: Int = 0): Result<List<VacancyWithDetails>>
    suspend fun countVacancies(filter: VacancyFilter): Result<Int>
    suspend fun getEmployerVacancies(userId: Long, limit: Int = 20, offset: Int = 0): Result<List<Vacancy>>
    suspend fun getCompanyVacancies(companyId: Long, limit: Int = 20, offset: Int = 0): Result<List<Vacancy>>
    suspend fun getRecentVacancies(limit: Int = 10): Result<List<VacancyWithDetails>>
    suspend fun updateVacancyStatus(userId: Long, id: Long, status: VacancyStatusCode): Result<Unit>
}
