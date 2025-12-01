package not.djinni.model.application

import kotlinx.datetime.Instant

data class Application(
    val id: Long,
    val vacancyId: Long,
    val jobSeekerId: Long,
    val statusCode: ApplicationStatusCode,
    val coverLetter: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
