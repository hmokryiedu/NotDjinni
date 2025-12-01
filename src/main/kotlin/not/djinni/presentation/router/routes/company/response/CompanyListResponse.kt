package not.djinni.presentation.router.routes.company.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompanyListResponse(
    @SerialName("companies")
    val companies: List<CompanyResponse>
)
