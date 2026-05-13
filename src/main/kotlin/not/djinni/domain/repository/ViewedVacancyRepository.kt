package not.djinni.domain.repository

import not.djinni.model.vacancy.ViewedVacancyWithDetails

interface ViewedVacancyRepository {
    suspend fun trackViewedVacancy(userId: Long, vacancyId: Long): Result<Unit>
    suspend fun getViewedVacancies(userId: Long, limit: Int = 20, offset: Int = 0): Result<List<ViewedVacancyWithDetails>>
}
