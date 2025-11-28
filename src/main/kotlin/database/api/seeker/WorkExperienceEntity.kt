package not.djinni.database.api.seeker

import java.time.LocalDate

data class WorkExperienceEntity(
    val id: Long = 0,
    val profileId: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?
)
