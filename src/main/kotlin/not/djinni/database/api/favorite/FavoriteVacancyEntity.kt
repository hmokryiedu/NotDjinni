package not.djinni.database.api.favorite

import kotlinx.datetime.Instant

data class FavoriteVacancyEntity(
    val id: Long = 0,
    val vacancyId: Long,
    val jobSeekerId: Long,
    val createdAt: Instant? = null,
)
