package not.djinni.database.impl.favorite

import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.vacancy.VacancyTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FavoriteVacancyTable : LongIdTable("favorite_vacancies", "id") {
    val vacancyId = reference("vacancy_id", VacancyTable, onDelete = ReferenceOption.CASCADE)
    val jobSeekerId = reference("job_seeker_id", SeekerProfileTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(vacancyId, jobSeekerId)
    }
}

class FavoriteVacancyTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FavoriteVacancyTableEntity>(FavoriteVacancyTable)

    var vacancyId by FavoriteVacancyTable.vacancyId
    var jobSeekerId by FavoriteVacancyTable.jobSeekerId
    var createdAt by FavoriteVacancyTable.createdAt
}

fun FavoriteVacancyTableEntity.toEntity() = FavoriteVacancyEntity(
    id = id.value,
    vacancyId = vacancyId.value,
    jobSeekerId = jobSeekerId.value,
    createdAt = createdAt,
)
