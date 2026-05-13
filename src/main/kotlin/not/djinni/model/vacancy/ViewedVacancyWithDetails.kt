package not.djinni.model.vacancy

import kotlinx.datetime.Instant

data class ViewedVacancyWithDetails(
    val viewedAt: Instant,
    val viewsCount: Int,
    val vacancy: VacancyWithDetails,
)
