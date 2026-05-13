package not.djinni.database.api.template

interface TemplateDao {
    suspend fun createTemplate(seekerId: Long, message: String): Long
    suspend fun getTemplates(seekerId: Long): List<TemplateEntity>
    suspend fun getTemplate(id: Long, seekerId: Long): TemplateEntity?
    suspend fun updateTemplate(id: Long, seekerId: Long, message: String): Boolean
    suspend fun deleteTemplate(id: Long, seekerId: Long): Boolean
}
