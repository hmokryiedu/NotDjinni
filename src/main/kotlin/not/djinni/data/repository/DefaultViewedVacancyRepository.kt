package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.viewed.ViewedVacancyDao
import not.djinni.database.api.viewed.ViewedVacancyEntity
import not.djinni.domain.exception.viewed.ViewedVacancyException
import not.djinni.domain.repository.ViewedVacancyRepository
import not.djinni.model.vacancy.VacancyStatusCode
import org.koin.core.annotation.Single

@Single(binds = [ViewedVacancyRepository::class])
class DefaultViewedVacancyRepository(
    private val viewedVacancyDao: ViewedVacancyDao,
    private val seekerProfileDao: SeekerProfileDao,
    private val vacancyDao: VacancyDao,
) : ViewedVacancyRepository {

    override suspend fun trackViewedVacancy(userId: Long, vacancyId: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw ViewedVacancyException.SeekerProfileNotFound()
        val vacancy = vacancyDao.getVacancy(vacancyId) ?: throw ViewedVacancyException.VacancyNotFound()
        if (vacancy.status != VacancyStatusCode.ACTIVE) {
            throw ViewedVacancyException.VacancyNotFound()
        }
        viewedVacancyDao.trackViewedVacancy(
            ViewedVacancyEntity(
                vacancyId = vacancyId,
                jobSeekerId = seekerProfile.id,
                viewedAt = Clock.System.now(),
            )
        )
    }

    override suspend fun getViewedVacancies(userId: Long, limit: Int, offset: Int) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw ViewedVacancyException.SeekerProfileNotFound()
        viewedVacancyDao.getViewedVacancies(
            jobSeekerId = seekerProfile.id,
            limit = limit,
            offset = offset,
        ).map { it.toDomain() }
    }
}
