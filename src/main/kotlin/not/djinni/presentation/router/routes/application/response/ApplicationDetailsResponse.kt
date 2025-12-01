package not.djinni.presentation.router.routes.application.response

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.routes.application.request.ApplicationStatusRequest
import not.djinni.presentation.router.routes.seeker.response.SeekerProfileResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyDetailsResponse

@Serializable
data class ApplicationDetailsResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("vacancy")
    val vacancy: VacancyDetailsResponse,
    @SerialName("job_seeker")
    val jobSeeker: SeekerProfileResponse,
    @SerialName("status")
    val status: ApplicationStatusRequest,
    @SerialName("cover_letter")
    val coverLetter: String?,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant
)
