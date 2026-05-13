package not.djinni.presentation.router.routes.template.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemplateRequest(
    @SerialName("message")
    val message: String,
)
