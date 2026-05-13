package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.domain.exception.favorite.FavoriteVacancyException
import not.djinni.domain.repository.FavoriteVacancyRepository
import not.djinni.model.vacancy.VacancyStatusCode
import org.koin.core.annotation.Single

@Single(binds = [FavoriteVacancyRepository::class])
class DefaultFavoriteVacancyRepository(
    private val favoriteVacancyDao: FavoriteVacancyDao,
    private val seekerProfileDao: SeekerProfileDao,
    private val vacancyDao: VacancyDao,
) : FavoriteVacancyRepository {

    override suspend fun addFavoriteVacancy(userId: Long, vacancyId: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.SeekerProfileNotFound()
        val vacancy = vacancyDao.getVacancy(vacancyId) ?: throw FavoriteVacancyException.VacancyNotFound()
        if (vacancy.status != VacancyStatusCode.ACTIVE) {
            throw FavoriteVacancyException.VacancyNotFound()
        }
        if (favoriteVacancyDao.favoriteExists(vacancyId = vacancyId, jobSeekerId = seekerProfile.id)) {
            throw FavoriteVacancyException.FavoriteAlreadyExists()
        }
        favoriteVacancyDao.addFavoriteVacancy(
            FavoriteVacancyEntity(
                vacancyId = vacancyId,
                jobSeekerId = seekerProfile.id,
                createdAt = Clock.System.now(),
            )
        )
    }

    override suspend fun removeFavoriteVacancy(userId: Long, vacancyId: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.SeekerProfileNotFound()
        if (!favoriteVacancyDao.favoriteExists(vacancyId = vacancyId, jobSeekerId = seekerProfile.id)) {
            throw FavoriteVacancyException.FavoriteNotFound()
        }
        favoriteVacancyDao.removeFavoriteVacancy(vacancyId = vacancyId, jobSeekerId = seekerProfile.id)
    }

    override suspend fun getFavoriteVacancies(userId: Long, limit: Int, offset: Int) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.SeekerProfileNotFound()
        favoriteVacancyDao.getFavoriteVacancies(
            jobSeekerId = seekerProfile.id,
            limit = limit,
            offset = offset,
        ).map { it.toDomain().copy(isFavorite = true) }
    }

    override suspend fun getFavoriteVacancyIds(userId: Long, vacancyIds: Set<Long>) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.SeekerProfileNotFound()
        favoriteVacancyDao.getFavoriteVacancyIds(
            jobSeekerId = seekerProfile.id,
            vacancyIds = vacancyIds,
        )
    }
}
