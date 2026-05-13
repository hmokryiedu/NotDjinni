package not.djinni.database.api.vacancy

import kotlinx.datetime.Instant
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode

data class VacancyEntity(
    val id: Long = 0,
    val companyId: Long,
    val title: String,
    val description: String,
    val salaryMin: Int,
    val salaryMax: Int,
    val minExperienceYears: Int?,
    val employmentType: EmploymentTypeCode?,
    val category: JobCategoryCode?,
    val status: VacancyStatusCode,
    val createdAt: Instant,
    val updatedAt: Instant,
    val applicationsCount: Int = 0,
    val isFavorite: Boolean = false,
)
