package not.djinni.presentation.router.routes.template.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemplateResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("message")
    val message: String,
)
