package not.djinni.data.mapper

import not.djinni.database.api.employer.CompanyEntity
import not.djinni.model.role.Company

fun CompanyEntity.toDomain() = Company(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)

fun Company.toEntity() = CompanyEntity(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)
