package not.djinni.database.impl.employer

import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object EmployerProfileTable : LongIdTable("employer_profiles", "id") {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val companyId = reference("company_id", CompanyTable)
    val role = varchar("role", MAX_VARCHAR_LENGTH)

    private const val MAX_VARCHAR_LENGTH = 255
}

class EmployerProfileTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EmployerProfileTableEntity>(EmployerProfileTable)

    var userId by EmployerProfileTable.userId
    var company by CompanyTableEntity referencedOn EmployerProfileTable.companyId
    var role by EmployerProfileTable.role
}

fun EmployerProfileTableEntity.toEntity() = EmployerProfileEntity(
    id = id.value,
    userId = userId.value,
    companyId = company.id.value,
    role = role
)
