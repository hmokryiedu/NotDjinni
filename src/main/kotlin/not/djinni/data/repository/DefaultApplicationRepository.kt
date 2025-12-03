package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.data.mapper.toDomain
import not.djinni.data.mapper.toEntity
import not.djinni.database.api.application.ApplicationDao
import not.djinni.database.api.application.ApplicationFilter
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.domain.exception.application.ApplicationException
import not.djinni.domain.repository.ApplicationRepository
import not.djinni.model.application.Application
import not.djinni.model.application.ApplicationStatusCode
import org.koin.core.annotation.Single

@Single(binds = [ApplicationRepository::class])
class DefaultApplicationRepository(
    private val applicationDao: ApplicationDao,
    private val seekerProfileDao: SeekerProfileDao,
    private val vacancyDao: VacancyDao,
    private val employerProfileDao: EmployerProfileDao
) : ApplicationRepository {

    override suspend fun createApplication(userId: Long, application: Application) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw ApplicationException.SeekerProfileNotFound()
        }
        val vacancy = vacancyDao.getVacancy(application.vacancyId) ?: run {
            throw ApplicationException.VacancyNotFound()
        }
        if (applicationDao.hasApplied(vacancyId = vacancy.id, jobSeekerId = seekerProfile.id)) {
            throw ApplicationException.AlreadyApplied()
        }
        val finalApplication = application.copy(
            jobSeekerId = seekerProfile.id,
            statusCode = ApplicationStatusCode.APPLIED,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val applicationId = applicationDao.createApplication(finalApplication.toEntity())
        applicationDao.getApplication(applicationId)?.application?.toDomain() ?: run {
            throw ApplicationException.ApplicationNotFound()
        }
    }

    override suspend fun getApplication(userId: Long, id: Long) = runCatching {
        val appWithDetails = applicationDao.getApplication(id) ?: run {
            throw ApplicationException.ApplicationNotFound()
        }
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId)
        val employerProfile = employerProfileDao.getProfileByUserId(userId)
        val isOwner = seekerProfile?.id == appWithDetails.application.jobSeekerId
        val isEmployer = employerProfile?.company?.id == appWithDetails.vacancy.company.id
        if (!isOwner && !isEmployer) throw ApplicationException.Unauthorized()
        appWithDetails.toDomain()
    }

    override suspend fun updateApplication(userId: Long, application: Application) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw ApplicationException.SeekerProfileNotFound()
        }
        val existing = applicationDao.getApplication(application.id) ?: run {
            throw ApplicationException.ApplicationNotFound()
        }
        if (existing.application.jobSeekerId != seekerProfile.id) throw ApplicationException.Unauthorized()
        val updated = applicationDao.updateApplication(
            application = existing.application.copy(
                coverLetter = application.coverLetter,
                updatedAt = Clock.System.now()
            )
        )
        if (!updated) throw ApplicationException.ApplicationNotFound()
    }

    override suspend fun deleteApplication(userId: Long, id: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw ApplicationException.SeekerProfileNotFound()
        }
        val existing = applicationDao.getApplication(id) ?: run {
            throw ApplicationException.ApplicationNotFound()
        }
        if (existing.application.jobSeekerId != seekerProfile.id) throw ApplicationException.Unauthorized()
        val deleted = applicationDao.deleteApplication(id)
        if (!deleted) throw ApplicationException.ApplicationNotFound()
    }

    override suspend fun updateApplicationStatus(
        userId: Long,
        id: Long,
        statusCode: ApplicationStatusCode
    ) = runCatching<Unit> {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw ApplicationException.Unauthorized()
        }
        val existing = applicationDao.getApplication(id) ?: run {
            throw ApplicationException.ApplicationNotFound()
        }
        if (existing.vacancy.company.id != employerProfile.company.id) throw ApplicationException.Unauthorized()
        val updated = applicationDao.updateApplicationStatus(id, statusCode)
        if (!updated) throw ApplicationException.ApplicationNotFound()
    }

    override suspend fun getMyApplications(
        userId: Long,
        filter: ApplicationFilter,
        limit: Int,
        offset: Int
    ) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw ApplicationException.SeekerProfileNotFound()
        }
        val seekerFilter = filter.copy(jobSeekerId = seekerProfile.id)
        applicationDao.getApplications(
            filter = seekerFilter,
            limit = limit,
            offset = offset
        ).map { it.toDomain() }
    }

    override suspend fun getVacancyApplications(
        userId: Long,
        vacancyId: Long,
        limit: Int,
        offset: Int
    ) = runCatching {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw ApplicationException.Unauthorized()
        }
        val vacancy = vacancyDao.getVacancy(vacancyId) ?: run {
            throw ApplicationException.VacancyNotFound()
        }
        if (vacancy.companyId != employerProfile.company.id) throw ApplicationException.Unauthorized()
        val filter = ApplicationFilter(vacancyId = vacancyId)
        applicationDao.getApplications(filter, limit, offset).map { it.toDomain() }
    }

    override suspend fun hasApplied(userId: Long, vacancyId: Long) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: return@runCatching false
        applicationDao.hasApplied(vacancyId, seekerProfile.id)
    }
}
