package not.djinni.database.impl.employer

import not.djinni.database.api.employer.CompanyEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object CompanyTable : LongIdTable("companies", "id") {
    val companyName = varchar("company_name", MAX_VARCHAR_LENGTH).uniqueIndex()
    val website = varchar("website", MAX_VARCHAR_LENGTH).nullable()
    val description = text("description")

    private const val MAX_VARCHAR_LENGTH = 255
}

class CompanyTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CompanyTableEntity>(CompanyTable)

    var companyName by CompanyTable.companyName
    var website by CompanyTable.website
    var description by CompanyTable.description
}

fun CompanyTableEntity.toEntity() = CompanyEntity(
    id = id.value,
    companyName = companyName,
    website = website,
    description = description
)
