package not.djinni.presentation.router.routes.employer.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.routes.company.response.CompanyResponse

@Serializable
data class EmployerProfileResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("role")
    val role: String,
    @SerialName("company")
    val company: CompanyResponse
)
