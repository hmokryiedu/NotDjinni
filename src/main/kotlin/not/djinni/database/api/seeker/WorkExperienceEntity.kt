package not.djinni.database.api.seeker

import kotlinx.datetime.Instant

data class WorkExperienceEntity(
    val id: Long = 0,
    val profileId: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: Instant,
    val endDate: Instant?
)
