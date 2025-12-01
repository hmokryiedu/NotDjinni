package not.djinni.presentation.router.routes.application.mapper

import io.ktor.http.*
import not.djinni.domain.exception.application.ApplicationException

fun ApplicationException.toStatusCode(): HttpStatusCode = when (this) {
    is ApplicationException.ApplicationNotFound -> HttpStatusCode.NotFound
    is ApplicationException.AlreadyApplied -> HttpStatusCode.Conflict
    is ApplicationException.Unauthorized -> HttpStatusCode.Forbidden
    is ApplicationException.VacancyNotFound -> HttpStatusCode.NotFound
    is ApplicationException.SeekerProfileNotFound -> HttpStatusCode.NotFound
    is ApplicationException.InvalidApplicationData -> HttpStatusCode.BadRequest
}
