package not.djinni.data.mapper

import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany as EmployerProfileWithCompanyEntity
import not.djinni.model.role.EmployerProfile
import not.djinni.model.role.EmployerProfileWithCompany

fun EmployerProfileWithCompanyEntity.toProfileDomain() = EmployerProfile(
    id = profile.id,
    companyId = profile.companyId,
    role = profile.role
)

fun EmployerProfileWithCompanyEntity.toProfileWithCompanyDomain() = EmployerProfileWithCompany(
    id = profile.id,
    role = profile.role,
    company = company.toDomain()
)

fun EmployerProfile.toEntity(userId: Long) = EmployerProfileEntity(
    id = id,
    userId = userId,
    companyId = companyId,
    role = role
)
