package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.favorite.FavoriteVacancyDao
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
    private val favoriteVacancyDao: FavoriteVacancyDao,
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
        val viewedVacancies = viewedVacancyDao.getViewedVacancies(
            jobSeekerId = seekerProfile.id,
            limit = limit,
            offset = offset,
        )
        val favoriteVacancyIds = favoriteVacancyDao.getFavoriteVacancyIds(
            jobSeekerId = seekerProfile.id,
            vacancyIds = viewedVacancies.map { it.vacancy.vacancy.id }.toSet(),
        )
        viewedVacancies.map { viewed ->
            viewed.copy(
                vacancy = viewed.vacancy.copy(
                    vacancy = viewed.vacancy.vacancy.copy(
                        isFavorite = favoriteVacancyIds.contains(viewed.vacancy.vacancy.id),
                    )
                )
            ).toDomain()
        }
    }
}
