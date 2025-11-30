package not.djinni.presentation.router.routes.employer.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEmployerProfileRequest(
    @SerialName("company_id")
    val companyId: Long,
    @SerialName("role")
    val role: String
)
