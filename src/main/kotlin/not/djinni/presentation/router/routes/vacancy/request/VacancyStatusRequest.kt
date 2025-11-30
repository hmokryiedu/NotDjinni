package not.djinni.presentation.router.routes.vacancy.request

import kotlinx.serialization.Serializable

@Serializable
enum class VacancyStatusRequest {
    DRAFT,
    ACTIVE,
    PAUSED,
    CLOSED,
    EXPIRED
}
