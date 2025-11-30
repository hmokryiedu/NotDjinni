package not.djinni.presentation.router.routes.vacancy.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateVacancyStatusRequest(
    @SerialName("status")
    val status: VacancyStatusRequest
)
