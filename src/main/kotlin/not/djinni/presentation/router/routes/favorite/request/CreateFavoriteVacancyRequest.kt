package not.djinni.presentation.router.routes.favorite.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFavoriteVacancyRequest(
    @SerialName("vacancy_id")
    val vacancyId: Long,
)
