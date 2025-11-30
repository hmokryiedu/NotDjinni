package not.djinni.presentation.router.routes.vacancy.mapper

import io.ktor.http.*
import not.djinni.domain.exception.vacancy.VacancyException

fun VacancyException.toStatusCode(): HttpStatusCode = when (this) {
    is VacancyException.VacancyNotFound -> HttpStatusCode.NotFound
    is VacancyException.Unauthorized -> HttpStatusCode.Forbidden
    is VacancyException.InvalidVacancyData -> HttpStatusCode.BadRequest
}
