package not.djinni.presentation.router.routes.vacancy.request

import kotlinx.serialization.Serializable

@Serializable
enum class EmploymentTypeRequest {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    TEMPORARY,
    INTERNSHIP,
    FREELANCE
}
