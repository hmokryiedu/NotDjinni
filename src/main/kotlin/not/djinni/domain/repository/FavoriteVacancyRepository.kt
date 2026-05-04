package not.djinni.domain.repository

import not.djinni.model.vacancy.VacancyWithDetails

interface FavoriteVacancyRepository {
    suspend fun addFavoriteVacancy(userId: Long, vacancyId: Long): Result<Unit>
    suspend fun removeFavoriteVacancy(userId: Long, vacancyId: Long): Result<Unit>
    suspend fun getFavoriteVacancies(userId: Long, limit: Int = 20, offset: Int = 0): Result<List<VacancyWithDetails>>
}
