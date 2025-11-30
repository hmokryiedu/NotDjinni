package not.djinni.presentation.router.routes.company

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.domain.exception.employer.CompanyException
import not.djinni.domain.repository.CompanyRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.company.mapper.toDomain
import not.djinni.presentation.router.routes.company.mapper.toResponse
import not.djinni.presentation.router.routes.company.mapper.toStatusCode
import not.djinni.presentation.router.routes.company.request.CreateCompanyRequest
import not.djinni.presentation.router.routes.company.request.UpdateCompanyRequest
import not.djinni.presentation.router.routes.company.resources.Company
import org.koin.core.annotation.Single

@Single
class CompanyRoute(
    private val companyRepository: CompanyRepository
) : Route {

    override fun install(root: Routing) = with(root) {
        getAllCompanies()
        getCompanyById()
        searchCompanies()
        createCompany()
        updateCompany()
        deleteCompany()
    }

    private fun Routing.getAllCompanies() {
        get<Company> {
            companyRepository.getAllCompanies()
                .onSuccess { companies ->
                    call.respond(companies.map { it.toResponse() })
                }
                .handleError(call = call, mapToCode = CompanyException::toStatusCode)
        }
    }

    private fun Routing.getCompanyById() {
        get<Company.ById> { resource ->
            companyRepository.getCompany(resource.id)
                .onSuccess { call.respond(it.toResponse()) }
                .handleError(call = call, mapToCode = CompanyException::toStatusCode)
        }
    }

    private fun Routing.searchCompanies() {
        get<Company.Search> { resource ->
            companyRepository.searchCompanies(resource.name)
                .onSuccess { companies ->
                    call.respond(companies.map { it.toResponse() })
                }
                .handleError(call = call, mapToCode = CompanyException::toStatusCode)
        }
    }

    private fun Routing.createCompany() {
        post<Company> {
            val request = call.receive<CreateCompanyRequest>()
            companyRepository.createCompany(request.toDomain())
                .onSuccess {
                    call.respond(status = HttpStatusCode.Created, message = it.toResponse())
                }
                .handleError(call = call, mapToCode = CompanyException::toStatusCode)
        }
    }

    private fun Routing.updateCompany() {
        put<Company.ById> { resource ->
            val request = call.receive<UpdateCompanyRequest>()
            companyRepository.updateCompany(request.toDomain(resource.id))
                .onSuccess {
                    call.respond("Company updated successfully".toMessageResponse())
                }
                .handleError(call = call, mapToCode = CompanyException::toStatusCode)
        }
    }

    private fun Routing.deleteCompany() {
        delete<Company.ById> { resource ->
            companyRepository.deleteCompany(resource.id)
                .onSuccess {
                    call.respond("Company deleted successfully".toMessageResponse())
                }
                .handleError(call = call, mapToCode = CompanyException::toStatusCode)
        }
    }
}
