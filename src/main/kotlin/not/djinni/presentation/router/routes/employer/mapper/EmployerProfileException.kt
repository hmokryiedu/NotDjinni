package not.djinni.presentation.router.routes.employer.mapper

import io.ktor.http.*
import not.djinni.domain.exception.employer.EmployerProfileException

fun EmployerProfileException.toStatusCode(): HttpStatusCode = when (this) {
    is EmployerProfileException.ProfileNotFound -> HttpStatusCode.NotFound
    is EmployerProfileException.ProfileAlreadyExists -> HttpStatusCode.Conflict
    is EmployerProfileException.CompanyNotFound -> HttpStatusCode.NotFound
    is EmployerProfileException.Unauthorized -> HttpStatusCode.Forbidden
    is EmployerProfileException.InvalidProfileData -> HttpStatusCode.BadRequest
}
