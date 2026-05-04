package not.djinni.presentation.router.routes.vacancy.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VacancyResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("company_id")
    val companyId: Long,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("salary_min")
    val salaryMin: Int,
    @SerialName("salary_max")
    val salaryMax: Int,
    @SerialName("min_experience_years")
    val minExperienceYears: Int?,
    @SerialName("employment_type")
    val employmentType: String?,
    @SerialName("category")
    val category: String?,
    @SerialName("status")
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("applications_count")
    val applicationsCount: Int
)
