package not.djinni.presentation.router.routes.template.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemplateListResponse(
    @SerialName("templates")
    val templates: List<TemplateResponse>,
)
