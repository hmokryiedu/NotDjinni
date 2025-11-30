package not.djinni.presentation.router.routes.company.mapper

import not.djinni.model.role.Company
import not.djinni.presentation.router.routes.company.request.CreateCompanyRequest
import not.djinni.presentation.router.routes.company.request.UpdateCompanyRequest
import not.djinni.presentation.router.routes.company.response.CompanyResponse

fun CreateCompanyRequest.toDomain() = Company(
    id = 0,
    companyName = companyName,
    website = website,
    description = description
)

fun UpdateCompanyRequest.toDomain(id: Long) = Company(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)

fun Company.toResponse() = CompanyResponse(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)
