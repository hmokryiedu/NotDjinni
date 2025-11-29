package not.djinni.presentation.router.routes.seeker.response

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkExperienceResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("company_name")
    val companyName: String,
    @SerialName("position")
    val position: String,
    @SerialName("description")
    val description: String?,
    @SerialName("start_date")
    @Contextual
    val startDate: Instant,
    @SerialName("end_date")
    @Contextual
    val endDate: Instant?,
    @SerialName("is_current")
    val isCurrent: Boolean
)
