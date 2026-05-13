package not.djinni.presentation.router.routes.vacancy.response

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.routes.company.response.CompanyResponse

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class VacancyDetailsResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("company")
    val company: CompanyResponse,
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
    val applicationsCount: Int,
    @EncodeDefault
    @SerialName("is_favorite")
    val isFavorite: Boolean = false,
)

@Serializable
data class VacancyGuestDetailsResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("company")
    val company: CompanyResponse,
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
    val applicationsCount: Int,
)
