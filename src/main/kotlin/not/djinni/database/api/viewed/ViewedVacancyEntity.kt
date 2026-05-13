package not.djinni.database.api.viewed

import kotlinx.datetime.Instant
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity

data class ViewedVacancyEntity(
    val id: Long = 0,
    val vacancyId: Long,
    val jobSeekerId: Long,
    val viewedAt: Instant,
)

data class ViewedVacancyWithDetailsEntity(
    val viewedAt: Instant,
    val viewsCount: Int,
    val vacancy: VacancyWithDetailsEntity,
)
