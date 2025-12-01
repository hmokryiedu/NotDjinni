package not.djinni.presentation.router.routes.application.mapper

import kotlinx.datetime.Clock
import not.djinni.model.application.Application
import not.djinni.model.application.ApplicationStatusCode
import not.djinni.model.application.ApplicationWithDetails
import not.djinni.presentation.router.routes.application.request.ApplicationStatusRequest
import not.djinni.presentation.router.routes.application.request.CreateApplicationRequest
import not.djinni.presentation.router.routes.application.request.UpdateApplicationRequest
import not.djinni.presentation.router.routes.application.response.ApplicationDetailsListResponse
import not.djinni.presentation.router.routes.application.response.ApplicationDetailsResponse
import not.djinni.presentation.router.routes.application.response.ApplicationListResponse
import not.djinni.presentation.router.routes.application.response.ApplicationResponse
import not.djinni.presentation.router.routes.seeker.mapper.toResponse
import not.djinni.presentation.router.routes.vacancy.mapper.toResponse

fun CreateApplicationRequest.toDomain() = Application(
    id = 0,
    vacancyId = vacancyId,
    jobSeekerId = 0, // Will be set by repository
    statusCode = ApplicationStatusCode.APPLIED,
    coverLetter = coverLetter,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now()
)

fun UpdateApplicationRequest.toDomain(id: Long, application: Application) = application.copy(
    id = id,
    coverLetter = coverLetter,
    updatedAt = Clock.System.now()
)

fun Application.toResponse() = ApplicationResponse(
    id = id,
    vacancyId = vacancyId,
    jobSeekerId = jobSeekerId,
    status = statusCode.toRequest(),
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ApplicationWithDetails.toResponse() = ApplicationDetailsResponse(
    id = id,
    vacancy = vacancy.toResponse(),
    jobSeeker = jobSeeker.toResponse(),
    status = statusCode.toRequest(),
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun List<Application>.toResponse(): ApplicationListResponse {
    return ApplicationListResponse(
        applications = this.map { it.toResponse() }
    )
}

fun List<ApplicationWithDetails>.toResponse(): ApplicationDetailsListResponse {
    return ApplicationDetailsListResponse(
        applications = this.map { it.toResponse() }
    )
}

fun ApplicationStatusCode.toRequest(): ApplicationStatusRequest = when (this) {
    ApplicationStatusCode.APPLIED -> ApplicationStatusRequest.APPLIED
    ApplicationStatusCode.REVIEWING -> ApplicationStatusRequest.REVIEWING
    ApplicationStatusCode.INTERVIEW -> ApplicationStatusRequest.INTERVIEW
    ApplicationStatusCode.TEST_TASK -> ApplicationStatusRequest.TEST_TASK
    ApplicationStatusCode.OFFER -> ApplicationStatusRequest.OFFER
    ApplicationStatusCode.HIRED -> ApplicationStatusRequest.HIRED
    ApplicationStatusCode.REJECTED -> ApplicationStatusRequest.REJECTED
    ApplicationStatusCode.WITHDRAWN -> ApplicationStatusRequest.WITHDRAWN
}

fun ApplicationStatusRequest.toDomain(): ApplicationStatusCode = when (this) {
    ApplicationStatusRequest.APPLIED -> ApplicationStatusCode.APPLIED
    ApplicationStatusRequest.REVIEWING -> ApplicationStatusCode.REVIEWING
    ApplicationStatusRequest.INTERVIEW -> ApplicationStatusCode.INTERVIEW
    ApplicationStatusRequest.TEST_TASK -> ApplicationStatusCode.TEST_TASK
    ApplicationStatusRequest.OFFER -> ApplicationStatusCode.OFFER
    ApplicationStatusRequest.HIRED -> ApplicationStatusCode.HIRED
    ApplicationStatusRequest.REJECTED -> ApplicationStatusCode.REJECTED
    ApplicationStatusRequest.WITHDRAWN -> ApplicationStatusCode.WITHDRAWN
}
