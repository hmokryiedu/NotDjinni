package not.djinni.presentation.router.routes.viewed.mapper

import io.ktor.http.HttpStatusCode
import not.djinni.domain.exception.viewed.ViewedVacancyException

fun ViewedVacancyException.toStatusCode(): HttpStatusCode = when (this) {
    is ViewedVacancyException.Unauthorized -> HttpStatusCode.Unauthorized
    is ViewedVacancyException.VacancyNotFound -> HttpStatusCode.NotFound
    is ViewedVacancyException.SeekerProfileNotFound -> HttpStatusCode.NotFound
}
