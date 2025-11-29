package not.djinni.database.impl.seeker

import not.djinni.database.api.seeker.WorkExperienceEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object WorkExperienceTable : LongIdTable("work_experience", "id") {
    val profileId = reference("profile_id", SeekerProfileTable)
    val companyName = varchar("company_name", 255)
    val position = varchar("position", 255)
    val description = text("description").nullable()
    val startDate = timestamp("start_date")
    val endDate = timestamp("end_date").nullable()
}

class WorkExperienceTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<WorkExperienceTableEntity>(WorkExperienceTable)

    var profileId by WorkExperienceTable.profileId
    var companyName by WorkExperienceTable.companyName
    var position by WorkExperienceTable.position
    var description by WorkExperienceTable.description
    var startDate by WorkExperienceTable.startDate
    var endDate by WorkExperienceTable.endDate
}

fun WorkExperienceTableEntity.toEntity() = WorkExperienceEntity(
    id = id.value,
    profileId = profileId.value,
    companyName = companyName,
    position = position,
    description = description,
    startDate = startDate,
    endDate = endDate
)
