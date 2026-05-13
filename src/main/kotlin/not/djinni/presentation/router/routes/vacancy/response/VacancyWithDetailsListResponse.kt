package not.djinni.presentation.router.routes.vacancy.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VacancyWithDetailsListResponse(
    @SerialName("vacancies")
    val vacancies: List<VacancyDetailsResponse>
)

@Serializable
data class VacancyGuestWithDetailsListResponse(
    @SerialName("vacancies")
    val vacancies: List<VacancyGuestDetailsResponse>
)
