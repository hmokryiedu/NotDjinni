package not.djinni.presentation.router.routes.seeker

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.domain.exception.seeker.SeekerProfileException
import not.djinni.domain.exception.viewed.ViewedVacancyException
import not.djinni.domain.repository.SeekerProfileRepository
import not.djinni.domain.repository.ViewedVacancyRepository
import not.djinni.domain.usecase.vacancy.GetRecommendedVacanciesForSeekerUseCase
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.seeker.mapper.toDomain
import not.djinni.presentation.router.routes.seeker.mapper.toResponse
import not.djinni.presentation.router.routes.seeker.mapper.toStatusCode
import not.djinni.presentation.router.routes.seeker.request.CreateProfileRequest
import not.djinni.presentation.router.routes.seeker.request.UpdateProfileRequest
import not.djinni.presentation.router.routes.seeker.request.WorkExperienceRequest
import not.djinni.presentation.router.routes.seeker.resources.Seeker
import not.djinni.presentation.router.routes.vacancy.mapper.toResponseList
import not.djinni.presentation.router.routes.vacancy.mapper.toViewedResponseList
import not.djinni.presentation.router.routes.viewed.mapper.toStatusCode
import org.koin.core.annotation.Single

@Single
class SeekerRoute(
    private val seekerProfileRepository: SeekerProfileRepository,
    private val getRecommendedVacanciesForSeekerUseCase: GetRecommendedVacanciesForSeekerUseCase,
    private val viewedVacancyRepository: ViewedVacancyRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        getProfile()
        createProfile()
        updateProfile()
        deleteProfile()
        addWorkExperience()
        updateWorkExperience()
        deleteWorkExperience()
        getRecommendedVacancies()
        getViewedVacancies()
    }

    private fun Routing.getViewedVacancies() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.ViewedVacancies> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                val queryParams = call.request.queryParameters
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT
                viewedVacancyRepository.getViewedVacancies(userId = userId, limit = limit, offset = offset)
                    .onSuccess { viewedVacancies -> call.respond(viewedVacancies.toViewedResponseList()) }
                    .handleError(call = call, mapToCode = ViewedVacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.getRecommendedVacancies() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.Vacancies> {
                val params = call.request.queryParameters
                val useCaseParams = GetRecommendedVacanciesForSeekerUseCase.Params(
                    userId = getUserIdFromTokenOrSendError() ?: return@get,
                    query = params["search"].orEmpty(),
                    limit = params[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT,
                    offset = params[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT,
                )
                getRecommendedVacanciesForSeekerUseCase(useCaseParams)
                    .onSuccess { vacancies -> call.respond(vacancies.toResponseList()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.getProfile() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                seekerProfileRepository.getProfile(userId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.createProfile() {
        authenticate(JwtAuth.NAME) {
            post<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateProfileRequest>()
                seekerProfileRepository.createProfile(userId, request.toDomain())
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = it.toResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.updateProfile() {
        authenticate(JwtAuth.NAME) {
            put<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateProfileRequest>()
                seekerProfileRepository.updateProfile(userId, request.toDomain())
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteProfile() {
        authenticate(JwtAuth.NAME) {
            delete<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                seekerProfileRepository.deleteProfile(userId)
                    .onSuccess { call.respond("Profile deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.addWorkExperience() {
        authenticate(JwtAuth.NAME) {
            post<Seeker.Experience> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<WorkExperienceRequest>()
                seekerProfileRepository.addWorkExperience(userId, request.toDomain())
                    .onSuccess { profile ->
                        call.respond(status = HttpStatusCode.Created, message = profile.toResponse())
                    }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.updateWorkExperience() {
        authenticate(JwtAuth.NAME) {
            put<Seeker.ExperienceById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<WorkExperienceRequest>()
                seekerProfileRepository.updateWorkExperience(
                    userId = userId,
                    experienceId = resource.id,
                    experience = request.toDomain()
                ).onSuccess {
                    call.respond(status = HttpStatusCode.OK, message = it.toResponse())
                }.handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteWorkExperience() {
        authenticate(JwtAuth.NAME) {
            delete<Seeker.ExperienceById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                seekerProfileRepository.deleteWorkExperience(userId, resource.id)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
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
