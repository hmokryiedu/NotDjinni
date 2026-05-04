package not.djinni.model.vacancy

import kotlinx.datetime.Instant

data class Vacancy(
    val id: Long,
    val companyId: Long,
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
