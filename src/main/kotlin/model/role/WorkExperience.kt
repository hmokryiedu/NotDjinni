package not.djinni.model.role

import java.time.LocalDate

data class WorkExperience(
    val id: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?
) {
    val isCurrent: Boolean
        get() = endDate == null
}
