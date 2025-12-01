package not.djinni.database.api.application

import kotlinx.datetime.Instant
import not.djinni.model.application.ApplicationStatusCode

data class ApplicationEntity(
    val id: Long = 0,
    val vacancyId: Long,
    val jobSeekerId: Long,
    val statusCode: ApplicationStatusCode,
    val coverLetter: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
