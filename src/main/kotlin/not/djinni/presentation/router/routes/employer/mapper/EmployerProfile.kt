package not.djinni.presentation.router.routes.employer.mapper

import not.djinni.model.role.EmployerProfile
import not.djinni.model.role.EmployerProfileWithCompany
import not.djinni.presentation.router.routes.company.mapper.toResponse
import not.djinni.presentation.router.routes.employer.request.CreateEmployerProfileRequest
import not.djinni.presentation.router.routes.employer.response.EmployerProfileResponse

fun CreateEmployerProfileRequest.toDomain() = EmployerProfile(
    id = 0,
    companyId = companyId,
    role = role
)

fun EmployerProfileWithCompany.toResponse() = EmployerProfileResponse(
    id = id,
    role = role,
    company = company.toResponse()
)
