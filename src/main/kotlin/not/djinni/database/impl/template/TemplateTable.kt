package not.djinni.database.impl.template

import not.djinni.database.api.template.TemplateEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TemplateTable : LongIdTable("templates", "id") {
    val seekerId = reference("seeker_id", SeekerProfileTable, onDelete = ReferenceOption.CASCADE)
    val message = text("message")
}

class TemplateTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TemplateTableEntity>(TemplateTable)

    var seekerId by TemplateTable.seekerId
    var message by TemplateTable.message
}

fun TemplateTableEntity.toEntity() = TemplateEntity(
    id = id.value,
    seekerId = seekerId.value,
    message = message,
)
