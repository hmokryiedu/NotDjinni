package not.djinni.model.vacancy

import kotlinx.datetime.Instant
import not.djinni.model.role.Company

data class VacancyWithDetails(
    val id: Long,
    val company: Company,
    val title: String,
    val description: String,
    val salary: Salary,
    val minExperienceYears: Int?,
    val employmentType: EmploymentTypeCode?,
    val category: JobCategoryCode?,
    val status: VacancyStatusCode,
    val createdAt: Instant,
    val updatedAt: Instant,
    val applicationsCount: Int = 0
)
