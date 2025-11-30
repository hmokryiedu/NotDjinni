package not.djinni.database.impl.vacancy

import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.impl.employer.CompanyTable
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object VacancyTable : LongIdTable("vacancies", "id") {
    val companyId = reference("company_id", CompanyTable)
    val title = varchar("title", 255)
    val description = text("description")
    val salaryMin = integer("salary_min")
    val salaryMax = integer("salary_max")
    val minExperienceYears = integer("min_experience_years").nullable()
    val employmentType = enumeration<EmploymentTypeCode>("employment_type").nullable()
    val category = enumeration<JobCategoryCode>("category").nullable()
    val status = enumeration<VacancyStatusCode>("status")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class VacancyTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<VacancyTableEntity>(VacancyTable)

    var companyId by VacancyTable.companyId
    var title by VacancyTable.title
    var description by VacancyTable.description
    var salaryMin by VacancyTable.salaryMin
    var salaryMax by VacancyTable.salaryMax
    var minExperienceYears by VacancyTable.minExperienceYears
    var employmentType by VacancyTable.employmentType
    var category by VacancyTable.category
    var status by VacancyTable.status
    var createdAt by VacancyTable.createdAt
    var updatedAt by VacancyTable.updatedAt
}

fun VacancyTableEntity.toEntity() = VacancyEntity(
    id = id.value,
    companyId = companyId.value,
    title = title,
    description = description,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    minExperienceYears = minExperienceYears,
    employmentType = employmentType,
    category = category,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
