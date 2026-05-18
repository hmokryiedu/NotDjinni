package not.djinni.presentation.router.routes.application

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.database.api.application.ApplicationFilter
import not.djinni.database.api.common.SortDirection
import not.djinni.database.api.application.ApplicationSortField
import not.djinni.domain.exception.application.ApplicationException
import not.djinni.domain.repository.ApplicationRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.application.mapper.*
import not.djinni.presentation.router.routes.application.request.ApplicationStatusRequest
import not.djinni.presentation.router.routes.application.request.CreateApplicationRequest
import not.djinni.presentation.router.routes.application.request.UpdateApplicationRequest
import not.djinni.presentation.router.routes.application.request.UpdateApplicationStatusRequest
import not.djinni.presentation.router.routes.application.resources.Application
import not.djinni.presentation.router.routes.application.response.HasAppliedResponse
import org.koin.core.annotation.Single

@Single
class ApplicationRoute(
    private val applicationRepository: ApplicationRepository
) : Route {

    override fun install(root: Routing) = with(root) {
        createApplication()
        getMyApplications()
        getApplicationById()
        getMyApplicationByVacancy()
        updateApplication()
        deleteApplication()
        withdrawApplication()
        updateApplicationStatus()
        getVacancyApplications()
        hasApplied()
    }

    private fun Routing.createApplication() {
        authenticate(JwtAuth.NAME) {
            post<Application> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateApplicationRequest>()
                applicationRepository.createApplication(userId = userId, application = request.toDomain())
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = it.toResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.getMyApplications() {
        authenticate(JwtAuth.NAME) {
            get<Application> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                val queryParams = call.request.queryParameters
                val filter = buildFilter(queryParams)
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT

                applicationRepository.getMyApplications(
                    userId = userId,
                    filter = filter,
                    limit = limit,
                    offset = offset
                )
                    .onSuccess { applications -> call.respond(applications.toResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.getApplicationById() {
        authenticate(JwtAuth.NAME) {
            get<Application.ById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                applicationRepository.getApplication(userId = userId, id = resource.id)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.getMyApplicationByVacancy() {
        authenticate(JwtAuth.NAME) {
            get<Application.MyByVacancy> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                applicationRepository.getMyApplicationByVacancy(userId = userId, vacancyId = resource.vacancyId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.updateApplication() {
        authenticate(JwtAuth.NAME) {
            put<Application.ById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateApplicationRequest>()
                val existingApp = applicationRepository.getApplication(
                    userId = userId,
                    id = resource.id
                ).getOrNull() ?: run {
                    call.respond(HttpStatusCode.NotFound, "Application not found".toMessageResponse())
                    return@put
                }

                val updatedApp = request.toDomain(
                    id = resource.id,
                    application = not.djinni.model.application.Application(
                        id = existingApp.id,
                        vacancyId = existingApp.vacancy.id,
                        jobSeekerId = existingApp.jobSeeker.id,
                        statusCode = existingApp.statusCode,
                        coverLetter = existingApp.coverLetter,
                        createdAt = existingApp.createdAt,
                        updatedAt = existingApp.updatedAt
                    )
                )

                applicationRepository.updateApplication(userId = userId, application = updatedApp)
                    .onSuccess { call.respond("Application updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteApplication() {
        authenticate(JwtAuth.NAME) {
            delete<Application.ById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                applicationRepository.deleteApplication(userId = userId, id = resource.id)
                    .onSuccess { call.respond("Application deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.withdrawApplication() {
        authenticate(JwtAuth.NAME) {
            patch<Application.Withdraw> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@patch
                applicationRepository.withdrawApplication(userId = userId, id = resource.id)
                    .onSuccess { call.respond("Application withdrawn successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.updateApplicationStatus() {
        authenticate(JwtAuth.NAME) {
            put<Application.Status> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateApplicationStatusRequest>()
                val statusCode = request.status.toDomain()
                applicationRepository.updateApplicationStatus(
                    userId = userId,
                    id = resource.id,
                    statusCode = statusCode
                )
                    .onSuccess { call.respond("Application status updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.getVacancyApplications() {
        authenticate(JwtAuth.NAME) {
            get<Application.ByVacancy> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                val queryParams = call.request.queryParameters
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT

                applicationRepository.getVacancyApplications(
                    userId = userId,
                    vacancyId = resource.vacancyId,
                    limit = limit,
                    offset = offset
                )
                    .onSuccess { applications -> call.respond(applications.toResponse()) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun Routing.hasApplied() {
        authenticate(JwtAuth.NAME) {
            get<Application.CheckByVacancy> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                applicationRepository.hasApplied(userId = userId, vacancyId = resource.vacancyId)
                    .onSuccess { call.respond(HasAppliedResponse(hasApplied = it)) }
                    .handleError(call = call, mapToCode = ApplicationException::toStatusCode)
            }
        }
    }

    private fun buildFilter(params: Parameters): ApplicationFilter {
        val statusCode = params.getFilter(
            key = "status",
            mapper = { ApplicationStatusRequest.valueOf(it).toDomain() }
        )

        val sortBy = params.getFilter(
            key = "sort_by",
            mapper = { ApplicationSortField.valueOf(it) },
        ) ?: ApplicationSortField.CREATED_AT

        val sortDirection = params.getFilter(
            key = "sort_direction",
            mapper = { SortDirection.valueOf(it) },
        ) ?: SortDirection.DESC

        return ApplicationFilter(
            statusCode = statusCode,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
    }

    private inline fun <reified T> Parameters.getFilter(
        key: String,
        mapper: (String) -> T?,
    ): T? = runCatching { get(key)?.let(mapper) }.getOrNull()

    private companion object {
        const val LIMIT_PARAM = "limit"
        const val OFFSET_PARAM = "offset"

        const val LIMIT_DEFAULT = 20
        const val OFFSET_DEFAULT = 0
    }
}
