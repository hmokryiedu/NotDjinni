package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.data.mapper.toDomain
import not.djinni.data.mapper.toEntity
import not.djinni.database.api.common.SortDirection
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancySortField
import not.djinni.domain.exception.vacancy.VacancyException
import not.djinni.domain.repository.VacancyRepository
import not.djinni.model.application.ApplicationStatusCode
import not.djinni.model.vacancy.Vacancy
import not.djinni.model.vacancy.VacancyStatusCode
import org.koin.core.annotation.Single

@Single(binds = [VacancyRepository::class])
class DefaultVacancyRepository(
    private val vacancyDao: VacancyDao,
    private val employerProfileDao: EmployerProfileDao,
    private val seekerProfileDao: SeekerProfileDao,
    private val favoriteVacancyDao: FavoriteVacancyDao,
) : VacancyRepository {

    override suspend fun createVacancy(userId: Long, vacancy: Vacancy) = runCatching {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No employer profile found")
        }
        val finalVacancy = vacancy.copy(
            status = vacancy.status,
            companyId = employerProfile.company.id,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val vacancyId = vacancyDao.createVacancy(finalVacancy.toEntity())
        vacancyDao.getVacancyWithDetails(vacancyId)?.toDomain() ?: run {
            throw VacancyException.VacancyNotFound()
        }
    }

    override suspend fun getVacancy(id: Long) = runCatching {
        vacancyDao.getVacancy(id)?.toDomain() ?: throw VacancyException.VacancyNotFound()
    }

    override suspend fun getVacancyWithDetails(id: Long) = runCatching {
        vacancyDao.getVacancyWithDetails(id)?.toDomain() ?: throw VacancyException.VacancyNotFound()
    }

    override suspend fun updateVacancy(userId: Long, vacancy: Vacancy) = runCatching<Unit> {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No employer profile found")
        }
        val existing = vacancyDao.getVacancy(vacancy.id) ?: run {
            throw VacancyException.VacancyNotFound()
        }
        if (existing.companyId != employerProfile.company.id) {
            throw VacancyException.Unauthorized("Cannot modify vacancy from different company")
        }
        val updated = vacancyDao.updateVacancy(vacancy.toEntity())
        if (!updated) throw VacancyException.VacancyNotFound()
    }

    override suspend fun deleteVacancy(userId: Long, id: Long) = runCatching<Unit> {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No employer profile found")
        }
        val existing = vacancyDao.getVacancy(id) ?: run {
            throw VacancyException.VacancyNotFound()
        }
        if (existing.companyId != employerProfile.company.id) {
            throw VacancyException.Unauthorized("Cannot delete vacancy from different company")
        }
        val deleted = vacancyDao.deleteVacancy(id)
        if (!deleted) throw VacancyException.VacancyNotFound()
    }

    override suspend fun getVacancies(filter: VacancyFilter, limit: Int, offset: Int) = runCatching {
        vacancyDao.getVacanciesWithDetails(filter, limit, offset).map { it.toDomain() }
    }

    override suspend fun getPublicVacancies(filter: VacancyFilter, limit: Int, offset: Int) = runCatching {
        vacancyDao.getVacanciesWithDetails(filter.forceActive(), limit, offset).map { it.toDomain() }
    }

    override suspend fun getPublicVacanciesForSeeker(
        userId: Long,
        filter: VacancyFilter,
        limit: Int,
        offset: Int,
    ) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No seeker profile found")
        }
        val vacancies = vacancyDao.getVacanciesWithDetails(filter.forceActive(), limit, offset)
        val favoriteVacancyIds = favoriteVacancyDao.getFavoriteVacancyIds(
            jobSeekerId = seekerProfile.id,
            vacancyIds = vacancies.map { it.vacancy.id }.toSet(),
        )
        vacancies.map { vacancy ->
            vacancy.copy(vacancy = vacancy.vacancy.copy(isFavorite = favoriteVacancyIds.contains(vacancy.vacancy.id))).toDomain()
        }
    }

    override suspend fun getAppliedVacancies(
        userId: Long,
        limit: Int,
        offset: Int,
        applicationStatuses: List<ApplicationStatusCode>,
    ) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No seeker profile found")
        }
        val vacancies = vacancyDao.getAppliedVacancies(
            jobSeekerId = seekerProfile.id,
            limit = limit,
            offset = offset,
            applicationStatuses = applicationStatuses,
        )
        val favoriteVacancyIds = favoriteVacancyDao.getFavoriteVacancyIds(
            jobSeekerId = seekerProfile.id,
            vacancyIds = vacancies.map { it.vacancy.id }.toSet(),
        )
        vacancies.map { vacancy ->
            vacancy.copy(vacancy = vacancy.vacancy.copy(isFavorite = favoriteVacancyIds.contains(vacancy.vacancy.id))).toDomain()
        }
    }

    override suspend fun getPublicVacancyWithDetails(id: Long) = runCatching {
        val vacancy = vacancyDao.getVacancyWithDetails(id)?.toDomain() ?: throw VacancyException.VacancyNotFound()
        if (vacancy.status != VacancyStatusCode.ACTIVE) throw VacancyException.VacancyNotFound()
        vacancy
    }

    override suspend fun getPublicVacancyWithDetailsForSeeker(userId: Long, id: Long) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId)
        val vacancy = vacancyDao.getVacancyWithDetails(id) ?: throw VacancyException.VacancyNotFound()
        if (vacancy.vacancy.status != VacancyStatusCode.ACTIVE) throw VacancyException.VacancyNotFound()
        if (seekerProfile == null) {
            return@runCatching vacancy.toDomain()
        }
        val favoriteVacancyIds = favoriteVacancyDao.getFavoriteVacancyIds(
            jobSeekerId = seekerProfile.id,
            vacancyIds = setOf(id),
        )
        vacancy.copy(vacancy = vacancy.vacancy.copy(isFavorite = favoriteVacancyIds.contains(id))).toDomain()
    }

    override suspend fun getPublicRecentVacancies(limit: Int) = runCatching {
        val filter = VacancyFilter(
            statuses = listOf(VacancyStatusCode.ACTIVE),
            sortBy = VacancySortField.CREATED_AT,
            sortDirection = SortDirection.DESC,
        )
        vacancyDao.getVacanciesWithDetails(filter, limit, offset = 0).map { it.toDomain() }
    }

    override suspend fun getPublicCompanyVacancies(companyId: Long, limit: Int, offset: Int) = runCatching {
        val filter = VacancyFilter(companyId = companyId).forceActive()
        vacancyDao.getVacanciesWithDetails(filter, limit, offset).map { it.toDomain() }
    }

    override suspend fun countVacancies(filter: VacancyFilter) = runCatching {
        vacancyDao.countVacancies(filter)
    }

    override suspend fun getEmployerVacancies(userId: Long, limit: Int, offset: Int) = runCatching {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No employer profile found")
        }
        vacancyDao
            .getVacanciesByCompany(companyId = employerProfile.company.id, limit = limit, offset = offset)
            .map { it.toDomain() }
    }

    override suspend fun getCompanyVacancies(companyId: Long, limit: Int, offset: Int) = runCatching {
        vacancyDao.getVacanciesByCompany(companyId, limit, offset).map { it.toDomain() }
    }

    override suspend fun getRecentVacancies(limit: Int) = runCatching {
        vacancyDao.getRecentVacancies(limit).mapNotNull { vacancy ->
            vacancyDao.getVacancyWithDetails(vacancy.id)?.toDomain()
        }
    }

    override suspend fun updateVacancyStatus(userId: Long, id: Long, status: VacancyStatusCode) = runCatching<Unit> {
        val employerProfile = employerProfileDao.getProfileByUserId(userId) ?: run {
            throw VacancyException.Unauthorized("No employer profile found")
        }
        val existing = vacancyDao.getVacancy(id) ?: run {
            throw VacancyException.VacancyNotFound()
        }
        if (existing.companyId != employerProfile.company.id) {
            throw VacancyException.Unauthorized("Cannot modify vacancy from different company")
        }
        val updated = vacancyDao.updateVacancyStatus(id, status)
        if (!updated) throw VacancyException.VacancyNotFound()
    }

    private fun VacancyFilter.forceActive() = copy(statuses = listOf(VacancyStatusCode.ACTIVE))
}
