package not.djinni.database.impl.viewed

import not.djinni.database.api.viewed.ViewedVacancyEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.vacancy.VacancyTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ViewedVacancyTable : LongIdTable("viewed_vacancies", "id") {
    val vacancyId = reference("vacancy_id", VacancyTable, onDelete = ReferenceOption.CASCADE)
    val jobSeekerId = reference("job_seeker_id", SeekerProfileTable, onDelete = ReferenceOption.CASCADE)
    val viewedAt = timestamp("viewed_at")

    init {
        uniqueIndex(vacancyId, jobSeekerId)
    }
}

class ViewedVacancyTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ViewedVacancyTableEntity>(ViewedVacancyTable)

    var vacancyId by ViewedVacancyTable.vacancyId
    var jobSeekerId by ViewedVacancyTable.jobSeekerId
    var viewedAt by ViewedVacancyTable.viewedAt
}

fun ViewedVacancyTableEntity.toEntity() = ViewedVacancyEntity(
    id = id.value,
    vacancyId = vacancyId.value,
    jobSeekerId = jobSeekerId.value,
    viewedAt = viewedAt,
)
