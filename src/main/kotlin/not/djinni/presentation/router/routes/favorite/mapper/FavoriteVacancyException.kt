package not.djinni.presentation.router.routes.favorite.mapper

import io.ktor.http.HttpStatusCode
import not.djinni.domain.exception.favorite.FavoriteVacancyException

fun FavoriteVacancyException.toStatusCode(): HttpStatusCode = when (this) {
    is FavoriteVacancyException.Unauthorized -> HttpStatusCode.Unauthorized
    is FavoriteVacancyException.VacancyNotFound -> HttpStatusCode.NotFound
    is FavoriteVacancyException.SeekerProfileNotFound -> HttpStatusCode.NotFound
    is FavoriteVacancyException.FavoriteAlreadyExists -> HttpStatusCode.Conflict
    is FavoriteVacancyException.FavoriteNotFound -> HttpStatusCode.Conflict
}
