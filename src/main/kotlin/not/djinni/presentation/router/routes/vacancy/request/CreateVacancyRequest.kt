package not.djinni.presentation.router.routes.vacancy.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVacancyRequest(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("salary_min")
    val salaryMin: Int,
    @SerialName("salary_max")
    val salaryMax: Int,
    @SerialName("min_experience_years")
    val minExperienceYears: Int? = null,
    @SerialName("employment_type")
    val employmentType: EmploymentTypeRequest? = null,
    @SerialName("category")
    val category: JobCategoryRequest? = null,
    @SerialName("status")
    val status: VacancyStatusRequest
)
