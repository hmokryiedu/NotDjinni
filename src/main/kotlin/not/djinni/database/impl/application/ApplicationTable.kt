package not.djinni.database.impl.application

import not.djinni.database.api.application.ApplicationEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.vacancy.VacancyTable
import not.djinni.model.application.ApplicationStatusCode
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ApplicationTable : LongIdTable("applications", "id") {
    val vacancyId = reference("vacancy_id", VacancyTable, onDelete = ReferenceOption.CASCADE)
    val jobSeekerId = reference("job_seeker_id", SeekerProfileTable, onDelete = ReferenceOption.CASCADE)
    val statusCode = enumeration<ApplicationStatusCode>("status")
    val coverLetter = text("cover_letter").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(vacancyId, jobSeekerId)
    }
}

class ApplicationTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ApplicationTableEntity>(ApplicationTable)

    var vacancyId by ApplicationTable.vacancyId
    var jobSeekerId by ApplicationTable.jobSeekerId
    var statusCode by ApplicationTable.statusCode
    var coverLetter by ApplicationTable.coverLetter
    var createdAt by ApplicationTable.createdAt
    var updatedAt by ApplicationTable.updatedAt
}

fun ApplicationTableEntity.toEntity() = ApplicationEntity(
    id = id.value,
    vacancyId = vacancyId.value,
    jobSeekerId = jobSeekerId.value,
    statusCode = statusCode,
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)
