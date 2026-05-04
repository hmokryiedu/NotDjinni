package not.djinni.data.repository

import kotlinx.datetime.Clock
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.domain.exception.favorite.FavoriteVacancyException
import not.djinni.domain.repository.FavoriteVacancyRepository
import org.koin.core.annotation.Single

@Single(binds = [FavoriteVacancyRepository::class])
class DefaultFavoriteVacancyRepository(
    private val favoriteVacancyDao: FavoriteVacancyDao,
    private val seekerProfileDao: SeekerProfileDao,
    private val vacancyDao: VacancyDao,
) : FavoriteVacancyRepository {

    override suspend fun addFavoriteVacancy(userId: Long, vacancyId: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.Unauthorized()
        if (!vacancyDao.vacancyExists(vacancyId)) throw FavoriteVacancyException.VacancyNotFound()
        if (favoriteVacancyDao.favoriteExists(vacancyId = vacancyId, jobSeekerId = seekerProfile.id)) return@runCatching
        favoriteVacancyDao.addFavoriteVacancy(
            FavoriteVacancyEntity(
                vacancyId = vacancyId,
                jobSeekerId = seekerProfile.id,
                createdAt = Clock.System.now(),
            )
        )
    }

    override suspend fun removeFavoriteVacancy(userId: Long, vacancyId: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.Unauthorized()
        favoriteVacancyDao.removeFavoriteVacancy(vacancyId = vacancyId, jobSeekerId = seekerProfile.id)
    }

    override suspend fun getFavoriteVacancies(userId: Long, limit: Int, offset: Int) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw FavoriteVacancyException.Unauthorized()
        favoriteVacancyDao.getFavoriteVacancies(
            jobSeekerId = seekerProfile.id,
            limit = limit,
            offset = offset,
        ).map { it.toDomain() }
    }
}
