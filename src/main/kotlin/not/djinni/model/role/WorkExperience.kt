package not.djinni.model.role

import kotlinx.datetime.Instant

data class WorkExperience(
    val id: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: Instant,
    val endDate: Instant?
) {
    val isCurrent: Boolean
        get() = endDate == null
}
