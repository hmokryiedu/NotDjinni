package not.djinni.database.api.favorite

import not.djinni.database.api.vacancy.VacancyWithDetailsEntity

interface FavoriteVacancyDao {
    suspend fun addFavoriteVacancy(favorite: FavoriteVacancyEntity): Boolean
    suspend fun removeFavoriteVacancy(vacancyId: Long, jobSeekerId: Long): Boolean
    suspend fun getFavoriteVacancies(jobSeekerId: Long, limit: Int = 20, offset: Int = 0): List<VacancyWithDetailsEntity>
    suspend fun favoriteExists(vacancyId: Long, jobSeekerId: Long): Boolean
    suspend fun getFavoriteVacancyIds(jobSeekerId: Long, vacancyIds: Set<Long>): Set<Long>
}
