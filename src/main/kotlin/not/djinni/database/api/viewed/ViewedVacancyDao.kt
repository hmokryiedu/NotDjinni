package not.djinni.database.api.viewed

interface ViewedVacancyDao {
    suspend fun trackViewedVacancy(viewedVacancy: ViewedVacancyEntity): Boolean
    suspend fun getViewedVacancies(jobSeekerId: Long, limit: Int = 20, offset: Int = 0): List<ViewedVacancyWithDetailsEntity>
    suspend fun countViewsByVacancyIds(vacancyIds: Set<Long>): Map<Long, Int>
}
