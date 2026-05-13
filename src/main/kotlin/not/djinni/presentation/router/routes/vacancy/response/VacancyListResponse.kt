package not.djinni.presentation.router.routes.vacancy.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VacancyListResponse(
    @SerialName("vacancies")
    val vacancies: List<VacancyResponse>
)

@Serializable
data class VacancyGuestListResponse(
    @SerialName("vacancies")
    val vacancies: List<VacancyGuestResponse>
)
