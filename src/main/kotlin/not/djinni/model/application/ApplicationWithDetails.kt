package not.djinni.model.application

import kotlinx.datetime.Instant
import not.djinni.model.role.SeekerProfile
import not.djinni.model.vacancy.VacancyWithDetails

data class ApplicationWithDetails(
    val id: Long,
    val vacancy: VacancyWithDetails,
    val jobSeeker: SeekerProfile,
    val statusCode: ApplicationStatusCode,
    val coverLetter: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
