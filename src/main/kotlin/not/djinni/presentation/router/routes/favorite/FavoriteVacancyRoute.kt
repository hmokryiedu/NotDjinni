package not.djinni.presentation.router.routes.favorite

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import not.djinni.domain.exception.favorite.FavoriteVacancyException
import not.djinni.domain.repository.FavoriteVacancyRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.favorite.mapper.toStatusCode
import not.djinni.presentation.router.routes.favorite.request.CreateFavoriteVacancyRequest
import not.djinni.presentation.router.routes.favorite.resources.FavoriteVacancy
import not.djinni.presentation.router.routes.vacancy.mapper.toResponseList
import org.koin.core.annotation.Single

@Single
class FavoriteVacancyRoute(
    private val favoriteVacancyRepository: FavoriteVacancyRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        addFavoriteVacancy()
        removeFavoriteVacancy()
        getFavoriteVacancies()
    }

    private fun Routing.addFavoriteVacancy() {
        authenticate(JwtAuth.NAME) {
            post<FavoriteVacancy> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateFavoriteVacancyRequest>()
                favoriteVacancyRepository.addFavoriteVacancy(userId = userId, vacancyId = request.vacancyId)
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = "Vacancy added to favorites".toMessageResponse()) }
                    .handleError(call = call, mapToCode = FavoriteVacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.removeFavoriteVacancy() {
        authenticate(JwtAuth.NAME) {
            delete<FavoriteVacancy.ByVacancy> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                favoriteVacancyRepository.removeFavoriteVacancy(userId = userId, vacancyId = resource.vacancyId)
                    .onSuccess { call.respond("Vacancy removed from favorites".toMessageResponse()) }
                    .handleError(call = call, mapToCode = FavoriteVacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.getFavoriteVacancies() {
        authenticate(JwtAuth.NAME) {
            get<FavoriteVacancy> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                val queryParams = call.request.queryParameters
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT
                favoriteVacancyRepository.getFavoriteVacancies(userId = userId, limit = limit, offset = offset)
                    .onSuccess { vacancies -> call.respond(vacancies.toResponseList()) }
                    .handleError(call = call, mapToCode = FavoriteVacancyException::toStatusCode)
            }
        }
    }

    private companion object {
        const val LIMIT_PARAM = "limit"
        const val OFFSET_PARAM = "offset"

        const val LIMIT_DEFAULT = 20
        const val OFFSET_DEFAULT = 0
    }
}
