package not.djinni.presentation.router.routes.vacancy

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.database.api.common.SortDirection
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancySortField
import not.djinni.domain.exception.vacancy.VacancyException
import not.djinni.domain.repository.VacancyRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getClaim
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.common.request.JobCategoryRequest
import not.djinni.presentation.router.routes.vacancy.mapper.*
import not.djinni.presentation.router.routes.vacancy.request.*
import not.djinni.presentation.router.routes.vacancy.resources.CompanyVacancies
import not.djinni.presentation.router.routes.vacancy.resources.Vacancy
import org.koin.core.annotation.Single

@Single
class VacancyRoute(
    private val vacancyRepository: VacancyRepository
) : Route {

    override fun install(root: Routing) = with(root) {
        listVacancies()
        getAppliedVacancies()
        getVacancyById()
        getRecentVacancies()
        getCompanyVacancies()
        createVacancy()
        updateVacancy()
        deleteVacancy()
        updateVacancyStatus()
    }

    private fun Routing.listVacancies() {
        authenticate(JwtAuth.NAME, optional = true) {
            get<Vacancy> {
                val queryParams = call.request.queryParameters
                val filter = buildFilter(queryParams)
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT
                val userId = getClaim<Long>(JwtAuth.USER_ID_CLAIM_NAME)
                if (userId == null) {
                    vacancyRepository.getPublicVacancies(filter = filter, limit = limit, offset = offset)
                        .onSuccess { vacancies -> call.respond(vacancies.toGuestResponseList()) }
                        .handleError(call = call, mapToCode = VacancyException::toStatusCode)
                } else {
                    vacancyRepository.getPublicVacanciesForSeeker(
                        userId = userId,
                        filter = filter,
                        limit = limit,
                        offset = offset
                    )
                        .onSuccess { vacancies -> call.respond(vacancies.toResponseList()) }
                        .handleError(call = call, mapToCode = VacancyException::toStatusCode)
                }
            }
        }
    }

    private fun Routing.getVacancyById() {
        authenticate(JwtAuth.NAME, optional = true) {
            get<Vacancy.ById> { resource ->
                val userId = getClaim<Long>(JwtAuth.USER_ID_CLAIM_NAME)
                if (userId == null) {
                    vacancyRepository.getPublicVacancyWithDetails(resource.id)
                        .onSuccess { call.respond(it.toGuestResponse()) }
                        .handleError(call = call, mapToCode = VacancyException::toStatusCode)
                } else {
                    vacancyRepository.getPublicVacancyWithDetailsForSeeker(userId, resource.id)
                        .onSuccess { call.respond(it.toResponse()) }
                        .handleError(call = call, mapToCode = VacancyException::toStatusCode)
                }
            }
        }
    }

    private fun Routing.getRecentVacancies() {
        get<Vacancy.Recent> { resource ->
            vacancyRepository.getPublicRecentVacancies(resource.limit)
                .onSuccess { vacancies -> call.respond(vacancies.toGuestResponseList()) }
                .handleError(call = call, mapToCode = VacancyException::toStatusCode)
        }
    }

    private fun Routing.getCompanyVacancies() {
        get<CompanyVacancies> { resource ->
            val queryParams = call.request.queryParameters
            val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
            val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT
            vacancyRepository.getPublicCompanyVacancies(resource.companyId, limit, offset)
                .onSuccess { vacancies -> call.respond(vacancies.toGuestResponseList()) }
                .handleError(call = call, mapToCode = VacancyException::toStatusCode)
        }
    }

    private fun Routing.getAppliedVacancies() {
        authenticate(JwtAuth.NAME) {
            get<Vacancy.Applied> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                val queryParams = call.request.queryParameters
                val limit = queryParams[LIMIT_PARAM]?.toIntOrNull() ?: LIMIT_DEFAULT
                val offset = queryParams[OFFSET_PARAM]?.toIntOrNull() ?: OFFSET_DEFAULT
                vacancyRepository.getAppliedVacancies(userId = userId, limit = limit, offset = offset)
                    .onSuccess { vacancies -> call.respond(vacancies.toResponseList()) }
                    .handleError(call = call, mapToCode = VacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.createVacancy() {
        authenticate(JwtAuth.NAME) {
            post<Vacancy> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateVacancyRequest>()
                vacancyRepository.createVacancy(userId = userId, vacancy = request.toDomain())
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = it.toResponse()) }
                    .handleError(call = call, mapToCode = VacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.updateVacancy() {
        authenticate(JwtAuth.NAME) {
            put<Vacancy.ById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateVacancyRequest>()
                val existingVacancy = vacancyRepository.getVacancy(id = resource.id).getOrNull() ?: run {
                    call.respond(HttpStatusCode.NotFound, "Vacancy not found".toMessageResponse())
                    return@put
                }
                val vacancy = request.toDomain(id = resource.id, companyId = existingVacancy.companyId)
                vacancyRepository.updateVacancy(userId = userId, vacancy = vacancy)
                    .onSuccess { call.respond("Vacancy updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = VacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteVacancy() {
        authenticate(JwtAuth.NAME) {
            delete<Vacancy.ById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                vacancyRepository.deleteVacancy(userId = userId, id = resource.id)
                    .onSuccess { call.respond("Vacancy deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = VacancyException::toStatusCode)
            }
        }
    }

    private fun Routing.updateVacancyStatus() {
        authenticate(JwtAuth.NAME) {
            put<Vacancy.Status> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateVacancyStatusRequest>()
                val status = request.status.toDomain()
                vacancyRepository.updateVacancyStatus(userId = userId, id = resource.id, status = status)
                    .onSuccess { call.respond("Vacancy status updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = VacancyException::toStatusCode)
            }
        }
    }

    private fun buildFilter(params: Parameters): VacancyFilter {
        val categories = params.getAllFilters(
            key = "category",
            mapper = { JobCategoryRequest.valueOf(it).toDomain() }
        )
        val statuses = params.getAllFilters(
            key = "status",
            mapper = { VacancyStatusRequest.valueOf(it).toDomain() }
        )
        val employmentTypes = params.getAllFilters(
            key = "employment_type",
            mapper = { EmploymentTypeRequest.valueOf(it).toDomain() }
        )
        val sortBy = params.getFilter(
            key = "sort_by",
            mapper = { VacancySortField.valueOf(it) },
        ) ?: VacancySortField.CREATED_AT

        val sortDirection = params.getFilter(
            key = "sort_direction",
            mapper = { SortDirection.valueOf(it) },
        ) ?: SortDirection.DESC

        return VacancyFilter(
            sortBy = sortBy,
            statuses = statuses,
            categories = categories,
            sortDirection = sortDirection,
            searchQuery = params["search"],
            employmentTypes = employmentTypes,
            companyId = params["company_id"]?.toLongOrNull(),
            salaryMin = params["salary_min"]?.toIntOrNull(),
            salaryMax = params["salary_max"]?.toIntOrNull(),
            experienceYears = params["experience_years"]?.toIntOrNull(),
        )
    }

    private inline fun <reified T> Parameters.getAllFilters(key: String, mapper: (String) -> T?): List<T> {
        return getAll(key)?.mapNotNull { runCatching { mapper(it) }.getOrNull() }.orEmpty()
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
