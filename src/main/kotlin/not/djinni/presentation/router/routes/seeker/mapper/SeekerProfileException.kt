package not.djinni.presentation.router.routes.seeker.mapper

import io.ktor.http.*
import not.djinni.domain.exception.seeker.SeekerProfileException

fun SeekerProfileException.toStatusCode(): HttpStatusCode = when (this) {
    is SeekerProfileException.ProfileNotFound -> HttpStatusCode.NotFound
    is SeekerProfileException.ProfileAlreadyExists -> HttpStatusCode.Conflict
    is SeekerProfileException.WorkExperienceNotFound -> HttpStatusCode.NotFound
    is SeekerProfileException.Unauthorized -> HttpStatusCode.Forbidden
    is SeekerProfileException.InvalidProfileData -> HttpStatusCode.BadRequest
}
