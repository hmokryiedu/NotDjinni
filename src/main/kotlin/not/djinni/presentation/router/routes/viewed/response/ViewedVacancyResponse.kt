package not.djinni.presentation.router.routes.viewed.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.routes.vacancy.response.VacancyDetailsResponse

@Serializable
data class ViewedVacancyResponse(
    @SerialName("viewed_at")
    val viewedAt: String,
    @SerialName("views_count")
    val viewsCount: Int,
    @SerialName("vacancy")
    val vacancy: VacancyDetailsResponse,
)

@Serializable
data class ViewedVacancyListResponse(
    @SerialName("viewed_vacancies")
    val viewedVacancies: List<ViewedVacancyResponse>,
)
