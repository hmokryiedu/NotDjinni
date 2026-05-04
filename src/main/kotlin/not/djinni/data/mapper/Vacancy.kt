package not.djinni.data.mapper

import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.model.vacancy.Salary
import not.djinni.model.vacancy.Vacancy
import not.djinni.model.vacancy.VacancyWithDetails

fun VacancyEntity.toDomain() = Vacancy(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salary = Salary(min = salaryMin, max = salaryMax),
    minExperienceYears = minExperienceYears,
    employmentType = employmentType,
    category = category,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    applicationsCount = applicationsCount
)

fun Vacancy.toEntity() = VacancyEntity(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salaryMin = salary.min,
    salaryMax = salary.max,
    minExperienceYears = minExperienceYears,
    employmentType = employmentType,
    category = category,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    applicationsCount = applicationsCount
)

fun VacancyWithDetailsEntity.toDomain() = VacancyWithDetails(
    id = vacancy.id,
    company = company.toDomain(),
    title = vacancy.title,
    description = vacancy.description,
    salary = Salary(min = vacancy.salaryMin, max = vacancy.salaryMax),
    minExperienceYears = vacancy.minExperienceYears,
    employmentType = vacancy.employmentType,
    category = vacancy.category,
    status = vacancy.status,
    createdAt = vacancy.createdAt,
    updatedAt = vacancy.updatedAt,
    applicationsCount = applicationsCount
)
