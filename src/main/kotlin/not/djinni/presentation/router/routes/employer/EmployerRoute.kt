package not.djinni.presentation.router.routes.employer

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.domain.exception.employer.EmployerProfileException
import not.djinni.domain.exception.vacancy.VacancyException
import not.djinni.domain.repository.EmployerProfileRepository
import not.djinni.domain.repository.VacancyRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.employer.mapper.toDomain
import not.djinni.presentation.router.routes.employer.mapper.toResponse
import not.djinni.presentation.router.routes.employer.mapper.toStatusCode
import not.djinni.presentation.router.routes.employer.request.CreateEmployerProfileRequest
import not.djinni.presentation.router.routes.employer.request.UpdateEmployerProfileRequest
import not.djinni.presentation.router.routes.employer.resources.Employer
import not.djinni.presentation.router.routes.vacancy.mapper.toResponseList
import not.djinni.presentation.router.routes.vacancy.mapper.toStatusCode
import org.koin.core.annotation.Single

@Single
class EmployerRoute(
    private val vacancyRepository: VacancyRepository,
    private val employerProfileRepository: EmployerProfileRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        getProfile()
        createProfile()
        updateProfile()
        deleteProfile()
        getEmployerVacancies()
    }

    private fun Routing.getProfile() {
        authenticate(JwtAuth.NAME) {
            get<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                employerProfileRepository.getProfile(userId)
                    .onSuccess { call.respond(message = it.toResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.createProfile() {
        authenticate(JwtAuth.NAME) {
            post<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateEmployerProfileRequest>()
                employerProfileRepository.createProfile(userId = userId, profile = request.toDomain())
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = it.toResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.updateProfile() {
        authenticate(JwtAuth.NAME) {
            put<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateEmployerProfileRequest>()
                employerProfileRepository.updateProfile(userId, request.role)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.getEmployerVacancies() {
        authenticate(JwtAuth.NAME) {
            get<Employer.Vacancies> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                val queryParams = call.request.queryParameters
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT
                val search = queryParams[SEARCH_PARAM]
                vacancyRepository.getEmployerVacancies(userId = userId, limit = limit, offset = offset, searchQuery = search)
                    .onSuccess { vacancies -> call.respond(vacancies.toResponseList()) }
                    .handleError(call = call, mapToCode = VacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteProfile() {
        authenticate(JwtAuth.NAME) {
            delete<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                employerProfileRepository.deleteProfile(userId)
                    .onSuccess { call.respond("Employer profile deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private companion object {
        const val LIMIT_PARAM = "limit"
        const val OFFSET_PARAM = "offset"
        const val SEARCH_PARAM = "search"

        const val LIMIT_DEFAULT = 20
        const val OFFSET_DEFAULT = 0
    }
}
