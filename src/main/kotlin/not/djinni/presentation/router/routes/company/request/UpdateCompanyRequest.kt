package not.djinni.presentation.router.routes.company.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCompanyRequest(
    @SerialName("company_name")
    val companyName: String,
    @SerialName("website")
    val website: String? = null,
    @SerialName("description")
    val description: String
)
