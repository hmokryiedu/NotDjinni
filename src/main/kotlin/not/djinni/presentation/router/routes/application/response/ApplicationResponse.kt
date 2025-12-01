package not.djinni.presentation.router.routes.application.response

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.routes.application.request.ApplicationStatusRequest

@Serializable
data class ApplicationResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("vacancy_id")
    val vacancyId: Long,
    @SerialName("job_seeker_id")
    val jobSeekerId: Long,
    @SerialName("status")
    val status: ApplicationStatusRequest,
    @SerialName("cover_letter")
    val coverLetter: String?,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant
)
