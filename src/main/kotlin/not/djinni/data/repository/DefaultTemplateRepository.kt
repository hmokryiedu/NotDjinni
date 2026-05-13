package not.djinni.data.repository

import not.djinni.data.mapper.toDomain
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.template.TemplateDao
import not.djinni.domain.exception.template.TemplateException
import not.djinni.domain.repository.TemplateRepository
import org.koin.core.annotation.Single

@Single(binds = [TemplateRepository::class])
class DefaultTemplateRepository(
    private val templateDao: TemplateDao,
    private val seekerProfileDao: SeekerProfileDao,
) : TemplateRepository {

    override suspend fun createTemplate(userId: Long, message: String) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw TemplateException.SeekerProfileNotFound()
        validateMessage(message)
        val id = templateDao.createTemplate(seekerId = seekerProfile.id, message = message)
        templateDao.getTemplate(id = id, seekerId = seekerProfile.id)?.toDomain()
            ?: throw TemplateException.TemplateNotFound()
    }

    override suspend fun getTemplates(userId: Long) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw TemplateException.SeekerProfileNotFound()
        templateDao.getTemplates(seekerId = seekerProfile.id).map { it.toDomain() }
    }

    override suspend fun getTemplate(userId: Long, id: Long) = runCatching {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw TemplateException.SeekerProfileNotFound()
        templateDao.getTemplate(id = id, seekerId = seekerProfile.id)?.toDomain()
            ?: throw TemplateException.TemplateNotFound()
    }

    override suspend fun updateTemplate(userId: Long, id: Long, message: String) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw TemplateException.SeekerProfileNotFound()
        validateMessage(message)
        if (!templateDao.updateTemplate(id = id, seekerId = seekerProfile.id, message = message)) {
            throw TemplateException.TemplateNotFound()
        }
    }

    override suspend fun deleteTemplate(userId: Long, id: Long) = runCatching<Unit> {
        val seekerProfile = seekerProfileDao.getProfileByUserId(userId) ?: throw TemplateException.SeekerProfileNotFound()
        if (!templateDao.deleteTemplate(id = id, seekerId = seekerProfile.id)) {
            throw TemplateException.TemplateNotFound()
        }
    }

    private fun validateMessage(message: String) {
        if (message.isBlank()) throw TemplateException.InvalidTemplateData()
    }
}
