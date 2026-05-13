package not.djinni.domain.repository

import not.djinni.model.template.Template

interface TemplateRepository {
    suspend fun createTemplate(userId: Long, message: String): Result<Template>
    suspend fun getTemplates(userId: Long): Result<List<Template>>
    suspend fun getTemplate(userId: Long, id: Long): Result<Template>
    suspend fun updateTemplate(userId: Long, id: Long, message: String): Result<Unit>
    suspend fun deleteTemplate(userId: Long, id: Long): Result<Unit>
}
