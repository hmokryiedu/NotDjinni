package not.djinni.database.impl.template

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.template.TemplateDao
import not.djinni.database.api.template.TemplateEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single([TemplateDao::class])
class DefaultTemplateDao : TemplateDao {

    override suspend fun createTemplate(seekerId: Long, message: String): Long = runQuery {
        TemplateTableEntity.new {
            this.seekerId = EntityID(seekerId, SeekerProfileTable)
            this.message = message
        }.id.value
    }

    override suspend fun getTemplates(seekerId: Long): List<TemplateEntity> = runQuery {
        TemplateTableEntity.find {
            TemplateTable.seekerId eq EntityID(seekerId, SeekerProfileTable)
        }
            .orderBy(TemplateTable.id to SortOrder.DESC)
            .map { it.toEntity() }
    }

    override suspend fun getTemplate(id: Long, seekerId: Long): TemplateEntity? = runQuery {
        TemplateTableEntity.find {
            (TemplateTable.id eq id) and
                    (TemplateTable.seekerId eq EntityID(seekerId, SeekerProfileTable))
        }.singleOrNull()?.toEntity()
    }

    override suspend fun updateTemplate(id: Long, seekerId: Long, message: String): Boolean = runQuery {
        TemplateTable.update({
            (TemplateTable.id eq id) and
                    (TemplateTable.seekerId eq EntityID(seekerId, SeekerProfileTable))
        }) {
            it[TemplateTable.message] = message
        } > 0
    }

    override suspend fun deleteTemplate(id: Long, seekerId: Long): Boolean = runQuery {
        TemplateTable.deleteWhere {
            (TemplateTable.id eq id) and
                    (TemplateTable.seekerId eq EntityID(seekerId, SeekerProfileTable))
        } > 0
    }
}
