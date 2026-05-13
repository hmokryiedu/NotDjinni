package not.djinni.database.impl.favorite

import kotlinx.datetime.Clock
import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.database.impl.employer.CompanyTableEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.vacancy.VacancyTable
import not.djinni.database.impl.vacancy.VacancyTableEntity
import not.djinni.database.impl.vacancy.countApplicationsByVacancyIds
import not.djinni.database.impl.vacancy.toEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.koin.core.annotation.Single

@Single([FavoriteVacancyDao::class])
class DefaultFavoriteVacancyDao : FavoriteVacancyDao {

    override suspend fun addFavoriteVacancy(favorite: FavoriteVacancyEntity): Boolean = runQuery {
        val exists = !FavoriteVacancyTableEntity.find {
            (FavoriteVacancyTable.vacancyId eq EntityID(favorite.vacancyId, VacancyTable)) and
                    (FavoriteVacancyTable.jobSeekerId eq EntityID(favorite.jobSeekerId, SeekerProfileTable))
        }.empty()
        if (exists) return@runQuery true
        FavoriteVacancyTableEntity.new {
            vacancyId = EntityID(favorite.vacancyId, VacancyTable)
            jobSeekerId = EntityID(favorite.jobSeekerId, SeekerProfileTable)
            createdAt = favorite.createdAt ?: Clock.System.now()
        }
        true
    }

    override suspend fun removeFavoriteVacancy(vacancyId: Long, jobSeekerId: Long): Boolean = runQuery {
        FavoriteVacancyTable.deleteWhere {
            (FavoriteVacancyTable.vacancyId eq EntityID(vacancyId, VacancyTable)) and
                    (FavoriteVacancyTable.jobSeekerId eq EntityID(jobSeekerId, SeekerProfileTable))
        } > 0
    }

    override suspend fun getFavoriteVacancies(
        jobSeekerId: Long,
        limit: Int,
        offset: Int,
    ): List<VacancyWithDetailsEntity> = runQuery {
        val favorites = FavoriteVacancyTableEntity.find {
            FavoriteVacancyTable.jobSeekerId eq EntityID(jobSeekerId, SeekerProfileTable)
        }
            .orderBy(FavoriteVacancyTable.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .toList()
        val applicationsCountByVacancyId = countApplicationsByVacancyIds(favorites.map { it.vacancyId.value })
        favorites
            .mapNotNull { favorite ->
                val vacancy = VacancyTableEntity.findById(favorite.vacancyId.value) ?: return@mapNotNull null
                val company = CompanyTableEntity.findById(vacancy.companyId.value) ?: return@mapNotNull null
                val applicationsCount = applicationsCountByVacancyId[vacancy.id.value] ?: 0
                VacancyWithDetailsEntity(
                    vacancy = vacancy.toEntity().copy(applicationsCount = applicationsCount),
                    company = CompanyEntity(
                        id = company.id.value,
                        companyName = company.companyName,
                        website = company.website,
                        description = company.description,
                    ),
                    applicationsCount = applicationsCount,
                )
            }
    }

    override suspend fun favoriteExists(vacancyId: Long, jobSeekerId: Long): Boolean = runQuery {
        !FavoriteVacancyTableEntity.find {
            (FavoriteVacancyTable.vacancyId eq EntityID(vacancyId, VacancyTable)) and
                    (FavoriteVacancyTable.jobSeekerId eq EntityID(jobSeekerId, SeekerProfileTable))
        }.empty()
    }
}
