package not.djinni.database.impl.application

import kotlinx.datetime.Clock
import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.application.*
import not.djinni.database.api.common.SortDirection
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.database.impl.employer.CompanyTableEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.seeker.SeekerProfileTableEntity
import not.djinni.database.impl.seeker.toEntity
import not.djinni.database.impl.vacancy.VacancyTable
import not.djinni.database.impl.vacancy.VacancyTableEntity
import not.djinni.database.impl.vacancy.countApplicationsByVacancyIds
import not.djinni.database.impl.vacancy.countViewsByVacancyIds
import not.djinni.database.impl.vacancy.toEntity
import not.djinni.model.application.ApplicationStatusCode
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single

@Single([ApplicationDao::class])
class DefaultApplicationDao : ApplicationDao {

    override suspend fun createApplication(application: ApplicationEntity): Long = runQuery {
        ApplicationTableEntity.new {
            vacancyId = EntityID(application.vacancyId, VacancyTable)
            jobSeekerId = EntityID(application.jobSeekerId, SeekerProfileTable)
            statusCode = application.statusCode
            coverLetter = application.coverLetter
            createdAt = application.createdAt
            updatedAt = application.updatedAt
        }.id.value
    }

    override suspend fun getApplication(id: Long): ApplicationWithDetailsEntity? = runQuery {
        val app = ApplicationTableEntity.findById(id) ?: return@runQuery null

        val vacancy = VacancyTableEntity.findById(app.vacancyId.value) ?: return@runQuery null
        val seeker = SeekerProfileTableEntity.findById(app.jobSeekerId.value) ?: return@runQuery null
        val company = CompanyTableEntity.findById(vacancy.companyId.value) ?: return@runQuery null
        val applicationsCount = countApplicationsByVacancyIds(listOf(vacancy.id.value))[vacancy.id.value] ?: 0
        val viewsCount = countViewsByVacancyIds(listOf(vacancy.id.value))[vacancy.id.value] ?: 0

        ApplicationWithDetailsEntity(
            application = app.toEntity(),
            jobSeeker = seeker.toEntity(),
            vacancy = VacancyWithDetailsEntity(
                vacancy = vacancy.toEntity().copy(
                    applicationsCount = applicationsCount,
                    viewsCount = viewsCount,
                ),
                company = CompanyEntity(
                    id = company.id.value,
                    companyName = company.companyName,
                    website = company.website,
                    description = company.description
                ),
                applicationsCount = applicationsCount,
                viewsCount = viewsCount,
            )
        )
    }

    override suspend fun updateApplication(application: ApplicationEntity): Boolean = runQuery {
        ApplicationTableEntity.findById(application.id)?.apply {
            statusCode = application.statusCode
            coverLetter = application.coverLetter
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun updateApplicationStatus(id: Long, statusCode: ApplicationStatusCode): Boolean = runQuery {
        ApplicationTableEntity.findById(id)?.apply {
            this.statusCode = statusCode
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun deleteApplication(id: Long): Boolean = runQuery {
        ApplicationTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun getApplications(
        filter: ApplicationFilter,
        limit: Int,
        offset: Int
    ): List<ApplicationWithDetailsEntity> = runQuery {
        val applications = filter.buildApplicationQuery()
            .limit(limit, offset.toLong())
            .map { ApplicationTableEntity.wrapRow(it) }
        val vacancyIds = applications.map { it.vacancyId.value }
        val applicationsCountByVacancyId = countApplicationsByVacancyIds(vacancyIds)
        val viewsCountByVacancyId = countViewsByVacancyIds(vacancyIds)
        applications
            .mapNotNull { row ->
                val app = row
                val vacancy = VacancyTableEntity.findById(app.vacancyId.value) ?: return@mapNotNull null
                val seeker = SeekerProfileTableEntity.findById(app.jobSeekerId.value) ?: return@mapNotNull null
                val company = CompanyTableEntity.findById(vacancy.companyId.value) ?: return@mapNotNull null
                val applicationsCount = applicationsCountByVacancyId[vacancy.id.value] ?: 0
                val viewsCount = viewsCountByVacancyId[vacancy.id.value] ?: 0

                ApplicationWithDetailsEntity(
                    application = app.toEntity(),
                    jobSeeker = seeker.toEntity(),
                    vacancy = VacancyWithDetailsEntity(
                        vacancy = vacancy.toEntity().copy(
                            applicationsCount = applicationsCount,
                            viewsCount = viewsCount,
                        ),
                        company = CompanyEntity(
                            id = company.id.value,
                            companyName = company.companyName,
                            website = company.website,
                            description = company.description
                        ),
                        applicationsCount = applicationsCount,
                        viewsCount = viewsCount,
                    )
                )
            }
    }

    override suspend fun countApplications(filter: ApplicationFilter): Int = runQuery {
        filter.buildApplicationQuery().count().toInt()
    }

    override suspend fun hasApplied(vacancyId: Long, jobSeekerId: Long): Boolean = runQuery {
        !ApplicationTableEntity.find {
            (ApplicationTable.vacancyId eq vacancyId) and (ApplicationTable.jobSeekerId eq jobSeekerId)
        }.empty()
    }

    private fun ApplicationFilter.buildApplicationQuery(): Query {
        val sortOrder = when (sortDirection) {
            SortDirection.ASC -> SortOrder.ASC
            SortDirection.DESC -> SortOrder.DESC
        }
        val sortColumn = when (sortBy) {
            ApplicationSortField.CREATED_AT -> ApplicationTable.createdAt
            ApplicationSortField.UPDATED_AT -> ApplicationTable.updatedAt
        }
        return if (companyId != null) {
            ApplicationTable.innerJoin(VacancyTable)
        } else {
            ApplicationTable
        }.selectAll().apply {
            if (companyId != null) andWhere { VacancyTable.companyId eq companyId }
            if (vacancyId != null) andWhere { ApplicationTable.vacancyId eq vacancyId }
            if (jobSeekerId != null) andWhere { ApplicationTable.jobSeekerId eq jobSeekerId }
            if (statusCode != null) andWhere { ApplicationTable.statusCode eq statusCode }
            orderBy(sortColumn to sortOrder)
        }
    }
}
