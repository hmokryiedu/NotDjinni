package not.djinni.database.impl.viewed

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.viewed.ViewedVacancyDao
import not.djinni.database.api.viewed.ViewedVacancyEntity
import not.djinni.database.api.viewed.ViewedVacancyWithDetailsEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.database.impl.employer.CompanyTable
import not.djinni.database.impl.employer.CompanyTableEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.vacancy.VacancyTable
import not.djinni.database.impl.vacancy.VacancyTableEntity
import not.djinni.database.impl.vacancy.countApplicationsByVacancyIds
import not.djinni.database.impl.vacancy.countViewsByVacancyIds as countViewsByVacancyIdsForVacancies
import not.djinni.database.impl.vacancy.toEntity
import not.djinni.model.vacancy.VacancyStatusCode
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single

@Single([ViewedVacancyDao::class])
class DefaultViewedVacancyDao : ViewedVacancyDao {

    override suspend fun trackViewedVacancy(viewedVacancy: ViewedVacancyEntity): Boolean = runQuery {
        try {
            upsertViewedVacancy(viewedVacancy)
        } catch (_: ExposedSQLException) {
            updateExistingViewedAt(viewedVacancy)
        }
        true
    }

    override suspend fun getViewedVacancies(
        jobSeekerId: Long,
        limit: Int,
        offset: Int,
    ): List<ViewedVacancyWithDetailsEntity> = runQuery {
        val rows = ViewedVacancyTable
            .join(VacancyTable, JoinType.INNER, ViewedVacancyTable.vacancyId, VacancyTable.id)
            .join(CompanyTable, JoinType.INNER, VacancyTable.companyId, CompanyTable.id)
            .selectAll()
            .where {
                (ViewedVacancyTable.jobSeekerId eq EntityID(jobSeekerId, SeekerProfileTable)) and
                    (VacancyTable.status eq VacancyStatusCode.ACTIVE)
            }
            .orderBy(ViewedVacancyTable.viewedAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .toList()
        val vacancyIds = rows.map { it[VacancyTable.id].value }.toSet()
        val applicationsCountByVacancyId = countApplicationsByVacancyIds(vacancyIds.toList())
        val viewsCountByVacancyId = countViewsByVacancyIdsForVacancies(vacancyIds)
        rows.map { row ->
            val vacancy = VacancyTableEntity.wrapRow(row)
            val company = CompanyTableEntity.wrapRow(row)
            val applicationsCount = applicationsCountByVacancyId[vacancy.id.value] ?: 0
            val viewsCount = viewsCountByVacancyId[vacancy.id.value] ?: 0
            ViewedVacancyWithDetailsEntity(
                viewedAt = row[ViewedVacancyTable.viewedAt],
                viewsCount = viewsCount,
                vacancy = VacancyWithDetailsEntity(
                    vacancy = vacancy.toEntity().copy(
                        applicationsCount = applicationsCount,
                        viewsCount = viewsCount,
                    ),
                    company = CompanyEntity(
                        id = company.id.value,
                        companyName = company.companyName,
                        website = company.website,
                        description = company.description,
                    ),
                    applicationsCount = applicationsCount,
                    viewsCount = viewsCount,
                ),
            )
        }
    }

    override suspend fun countViewsByVacancyIds(vacancyIds: Set<Long>): Map<Long, Int> = runQuery {
        countViewsByVacancyIdsForVacancies(vacancyIds)
    }

    private fun upsertViewedVacancy(viewedVacancy: ViewedVacancyEntity) {
        val existing = findViewedVacancy(viewedVacancy)
        if (existing == null) {
            ViewedVacancyTableEntity.new {
                vacancyId = EntityID(viewedVacancy.vacancyId, VacancyTable)
                jobSeekerId = EntityID(viewedVacancy.jobSeekerId, SeekerProfileTable)
                viewedAt = viewedVacancy.viewedAt
            }
        } else {
            existing.viewedAt = viewedVacancy.viewedAt
        }
    }

    private fun updateExistingViewedAt(viewedVacancy: ViewedVacancyEntity) {
        findViewedVacancy(viewedVacancy)?.viewedAt = viewedVacancy.viewedAt
    }

    private fun findViewedVacancy(viewedVacancy: ViewedVacancyEntity): ViewedVacancyTableEntity? {
        return ViewedVacancyTableEntity.find {
            (ViewedVacancyTable.vacancyId eq EntityID(viewedVacancy.vacancyId, VacancyTable)) and
                (ViewedVacancyTable.jobSeekerId eq EntityID(viewedVacancy.jobSeekerId, SeekerProfileTable))
        }.firstOrNull()
    }
}
