package not.djinni.database.impl.vacancy

import kotlinx.datetime.Clock
import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.vacancy.*
import not.djinni.database.impl.employer.CompanyTable
import not.djinni.database.impl.employer.CompanyTableEntity
import not.djinni.database.impl.employer.toEntity
import not.djinni.model.vacancy.VacancyStatusCode
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single

@Single([VacancyDao::class])
class DefaultVacancyDao : VacancyDao {

    override suspend fun createVacancy(vacancy: VacancyEntity): Long = runQuery {
        VacancyTableEntity.new {
            companyId = EntityID(vacancy.companyId, CompanyTable)
            title = vacancy.title
            description = vacancy.description
            salaryMin = vacancy.salaryMin
            salaryMax = vacancy.salaryMax
            minExperienceYears = vacancy.minExperienceYears
            employmentType = vacancy.employmentType
            category = vacancy.category
            status = vacancy.status
            createdAt = vacancy.createdAt
            updatedAt = vacancy.updatedAt
        }.id.value
    }

    override suspend fun getVacancy(id: Long): VacancyEntity? = runQuery {
        VacancyTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getVacancyWithDetails(id: Long): VacancyWithDetailsEntity? = runQuery {
        val vacancyTableEntity = VacancyTableEntity.findById(id) ?: return@runQuery null
        val company = CompanyTableEntity.findById(vacancyTableEntity.companyId.value) ?: return@runQuery null
        VacancyWithDetailsEntity(
            vacancy = vacancyTableEntity.toEntity(),
            company = company.toEntity()
        )
    }

    override suspend fun updateVacancy(vacancy: VacancyEntity): Boolean = runQuery {
        VacancyTableEntity.findById(vacancy.id)?.apply {
            title = vacancy.title
            description = vacancy.description
            salaryMin = vacancy.salaryMin
            salaryMax = vacancy.salaryMax
            minExperienceYears = vacancy.minExperienceYears
            employmentType = vacancy.employmentType
            category = vacancy.category
            status = vacancy.status
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun deleteVacancy(id: Long): Boolean = runQuery {
        VacancyTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun getVacancies(filter: VacancyFilter, limit: Int, offset: Int): List<VacancyEntity> = runQuery {
        filter.buildVacancyQuery()
            .limit(n = limit, offset = offset.toLong())
            .map { VacancyTableEntity.wrapRow(it).toEntity() }
    }

    override suspend fun getVacanciesWithDetails(
        filter: VacancyFilter,
        limit: Int,
        offset: Int
    ): List<VacancyWithDetailsEntity> = runQuery {
        filter.buildVacancyQuery()
            .limit(n = limit, offset = offset.toLong())
            .map {
                val vacancy = VacancyTableEntity.wrapRow(it)
                val company = CompanyTableEntity.findById(vacancy.companyId.value) ?: run {
                    error("Company not found for vacancy id=${vacancy.id.value}")
                }
                VacancyWithDetailsEntity(
                    vacancy = vacancy.toEntity(),
                    company = CompanyEntity(
                        id = company.id.value,
                        companyName = company.companyName,
                        website = company.website,
                        description = company.description
                    )
                )
            }
    }

    override suspend fun countVacancies(filter: VacancyFilter): Int = runQuery {
        filter.buildVacancyQuery().count().toInt()
    }

    override suspend fun getVacanciesByCompany(
        companyId: Long,
        limit: Int,
        offset: Int
    ): List<VacancyEntity> = runQuery {
        VacancyTableEntity.find { VacancyTable.companyId eq EntityID(companyId, CompanyTable) }
            .orderBy(VacancyTable.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { it.toEntity() }
    }

    override suspend fun getRecentVacancies(limit: Int): List<VacancyEntity> = runQuery {
        VacancyTableEntity.all()
            .orderBy(VacancyTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .map { it.toEntity() }
    }

    override suspend fun updateVacancyStatus(id: Long, status: VacancyStatusCode): Boolean = runQuery {
        VacancyTableEntity.findById(id)?.apply {
            this.status = status
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun vacancyExists(id: Long): Boolean = runQuery {
        VacancyTableEntity.findById(id) != null
    }

    private fun VacancyFilter.buildVacancyQuery(): Query {
        val sortOrder = when (sortDirection) {
            SortDirection.ASC -> SortOrder.ASC
            SortDirection.DESC -> SortOrder.DESC
        }
        val sortColumn = when (sortBy) {
            VacancySortField.CREATED_AT -> VacancyTable.createdAt
            VacancySortField.UPDATED_AT -> VacancyTable.updatedAt
            VacancySortField.SALARY_MIN -> VacancyTable.salaryMin
            VacancySortField.SALARY_MAX -> VacancyTable.salaryMax
            VacancySortField.TITLE -> VacancyTable.title
            VacancySortField.MIN_EXPERIENCE -> VacancyTable.minExperienceYears
        }
        return VacancyTable.selectAll().apply {
            if (companyId != null) andWhere { VacancyTable.companyId eq EntityID(companyId, CompanyTable) }
            if (categories.isNotEmpty()) andWhere { VacancyTable.category inList categories }
            if (statuses.isNotEmpty()) andWhere { VacancyTable.status inList statuses }
            if (employmentTypes.isNotEmpty()) andWhere { VacancyTable.employmentType inList employmentTypes }
            if (salaryMax != null) andWhere { VacancyTable.salaryMax lessEq salaryMax }
            if (salaryMin != null) andWhere { VacancyTable.salaryMin greaterEq salaryMin }
            if (minExperienceYears != null) andWhere { VacancyTable.minExperienceYears greaterEq minExperienceYears }
            if (maxExperienceYears != null) andWhere { VacancyTable.minExperienceYears lessEq maxExperienceYears }
            if (searchQuery != null) {
                andWhere { (VacancyTable.title like "%$searchQuery%") or (VacancyTable.description like "%$searchQuery%") }
            }
            orderBy(sortColumn to sortOrder)
        }
    }
}
