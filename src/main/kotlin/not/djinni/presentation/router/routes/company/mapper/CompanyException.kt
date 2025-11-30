package not.djinni.presentation.router.routes.company.mapper

import io.ktor.http.*
import not.djinni.domain.exception.employer.CompanyException

fun CompanyException.toStatusCode(): HttpStatusCode = when (this) {
    is CompanyException.CompanyNotFound -> HttpStatusCode.NotFound
    is CompanyException.CompanyAlreadyExists -> HttpStatusCode.Conflict
    is CompanyException.InvalidCompanyData -> HttpStatusCode.BadRequest
}
