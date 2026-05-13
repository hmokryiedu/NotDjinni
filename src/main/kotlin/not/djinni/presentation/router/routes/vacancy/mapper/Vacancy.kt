package not.djinni.presentation.router.routes.vacancy.mapper

import kotlinx.datetime.Clock
import not.djinni.model.vacancy.*
import not.djinni.presentation.router.routes.company.mapper.toResponse
import not.djinni.presentation.router.routes.vacancy.request.CreateVacancyRequest
import not.djinni.presentation.router.routes.vacancy.request.EmploymentTypeRequest
import not.djinni.presentation.router.routes.common.request.JobCategoryRequest
import not.djinni.presentation.router.routes.vacancy.request.UpdateVacancyRequest
import not.djinni.presentation.router.routes.vacancy.request.VacancyStatusRequest
import not.djinni.presentation.router.routes.vacancy.response.VacancyDetailsResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyGuestDetailsResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyGuestListResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyGuestResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyGuestWithDetailsListResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyListResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyResponse
import not.djinni.presentation.router.routes.vacancy.response.VacancyWithDetailsListResponse

fun EmploymentTypeRequest.toDomain() = EmploymentTypeCode.valueOf(name)

fun JobCategoryRequest.toDomain() = JobCategoryCode.valueOf(name)

fun VacancyStatusRequest.toDomain() = VacancyStatusCode.valueOf(name)

fun EmploymentTypeCode.toRequest() = EmploymentTypeRequest.valueOf(name)

fun JobCategoryCode.toRequest() = JobCategoryRequest.valueOf(name)

fun VacancyStatusCode.toRequest() = VacancyStatusRequest.valueOf(name)

fun CreateVacancyRequest.toDomain() = Vacancy(
    id = 0,
    companyId = 0,
    title = title,
    description = description,
    salary = Salary(min = salaryMin, max = salaryMax),
    minExperienceYears = minExperienceYears,
    employmentType = employmentType?.toDomain(),
    category = category?.toDomain(),
    status = status.toDomain(),
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now()
)

fun UpdateVacancyRequest.toDomain(id: Long, companyId: Long) = Vacancy(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salary = Salary(min = salaryMin, max = salaryMax),
    minExperienceYears = minExperienceYears,
    employmentType = employmentType?.toDomain(),
    category = category?.toDomain(),
    status = status.toDomain(),
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now()
)

fun Vacancy.toResponse() = VacancyResponse(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salaryMin = salary.min,
    salaryMax = salary.max,
    minExperienceYears = minExperienceYears,
    employmentType = employmentType?.toRequest()?.name,
    category = category?.toRequest()?.name,
    status = status.toRequest().name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    applicationsCount = applicationsCount,
    isFavorite = isFavorite,
)

fun Vacancy.toGuestResponse() = VacancyGuestResponse(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salaryMin = salary.min,
    salaryMax = salary.max,
    minExperienceYears = minExperienceYears,
    employmentType = employmentType?.toRequest()?.name,
    category = category?.toRequest()?.name,
    status = status.toRequest().name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    applicationsCount = applicationsCount,
)

fun VacancyWithDetails.toResponse() = VacancyDetailsResponse(
    id = id,
    company = company.toResponse(),
    title = title,
    description = description,
    salaryMin = salary.min,
    salaryMax = salary.max,
    minExperienceYears = minExperienceYears,
    employmentType = employmentType?.toRequest()?.name,
    category = category?.toRequest()?.name,
    status = status.toRequest().name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    applicationsCount = applicationsCount,
    isFavorite = isFavorite,
)

fun VacancyWithDetails.toGuestResponse() = VacancyGuestDetailsResponse(
    id = id,
    company = company.toResponse(),
    title = title,
    description = description,
    salaryMin = salary.min,
    salaryMax = salary.max,
    minExperienceYears = minExperienceYears,
    employmentType = employmentType?.toRequest()?.name,
    category = category?.toRequest()?.name,
    status = status.toRequest().name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    applicationsCount = applicationsCount,
)

fun List<Vacancy>.toResponseList() = VacancyListResponse(
    vacancies = map { it.toResponse() }
)

fun List<Vacancy>.toGuestResponseList() = VacancyGuestListResponse(
    vacancies = map { it.toGuestResponse() }
)

fun List<VacancyWithDetails>.toResponseList() = VacancyWithDetailsListResponse(
    vacancies = map { it.toResponse() }
)

fun List<VacancyWithDetails>.toGuestResponseList() = VacancyGuestWithDetailsListResponse(
    vacancies = map { it.toGuestResponse() }
)
