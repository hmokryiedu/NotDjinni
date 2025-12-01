package not.djinni.data.mapper

import not.djinni.database.api.application.ApplicationEntity
import not.djinni.database.api.application.ApplicationWithDetailsEntity
import not.djinni.model.application.Application
import not.djinni.model.application.ApplicationWithDetails

fun ApplicationEntity.toDomain() = Application(
    id = id,
    vacancyId = vacancyId,
    jobSeekerId = jobSeekerId,
    statusCode = statusCode,
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Application.toEntity() = ApplicationEntity(
    id = id,
    vacancyId = vacancyId,
    jobSeekerId = jobSeekerId,
    statusCode = statusCode,
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ApplicationWithDetailsEntity.toDomain() = ApplicationWithDetails(
    id = application.id,
    vacancy = vacancy.toDomain(),
    jobSeeker = jobSeeker.toDomain(),
    statusCode = application.statusCode,
    coverLetter = application.coverLetter,
    createdAt = application.createdAt,
    updatedAt = application.updatedAt
)
