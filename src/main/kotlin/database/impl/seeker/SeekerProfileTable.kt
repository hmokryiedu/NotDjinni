package not.djinni.database.impl.seeker

import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object SeekerProfileTable : LongIdTable("job_seeker_profiles", "id") {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val specialty = varchar("specialty", 255)
    val experienceYears = integer("experience_years")
    val desiredSalary = integer("desired_salary")
    val aboutMe = text("about_me").nullable()
}

class SeekerProfileTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SeekerProfileTableEntity>(SeekerProfileTable)

    var userId by SeekerProfileTable.userId
    var specialty by SeekerProfileTable.specialty
    var experienceYears by SeekerProfileTable.experienceYears
    var desiredSalary by SeekerProfileTable.desiredSalary
    var aboutMe by SeekerProfileTable.aboutMe
}

fun SeekerProfileTableEntity.toEntity() = SeekerProfileEntity(
    id = id.value,
    userId = userId.value,
    specialty = specialty,
    experienceYears = experienceYears,
    desiredSalary = desiredSalary,
    aboutMe = aboutMe
)
