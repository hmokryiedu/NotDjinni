package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.database.api.template.TemplateDao
import not.djinni.database.api.template.TemplateEntity
import not.djinni.domain.exception.template.TemplateException
import not.djinni.model.vacancy.JobCategoryCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultTemplateRepositoryTest {

    @Test
    fun `createTemplate stores template for current seeker profile`() {
        runBlocking {
            val templateDao = FakeTemplateDao()
            val repository = repository(templateDao = templateDao)

            val result = repository.createTemplate(userId = USER_ID, message = MESSAGE)

            assertTrue(result.isSuccess)
            assertEquals(TEMPLATE_ID, result.getOrThrow().id)
            assertEquals(MESSAGE, result.getOrThrow().message)
            assertEquals(SEEKER_ID to MESSAGE, templateDao.created.single())
        }
    }

    @Test
    fun `createTemplate returns SeekerProfileNotFound when profile is missing`() {
        runBlocking {
            val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

            val result = repository.createTemplate(userId = USER_ID, message = MESSAGE)

            assertIs<TemplateException.SeekerProfileNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `createTemplate rejects empty message`() {
        runBlocking {
            val templateDao = FakeTemplateDao()
            val repository = repository(templateDao = templateDao)

            val result = repository.createTemplate(userId = USER_ID, message = "")

            assertIs<TemplateException.InvalidTemplateData>(result.exceptionOrNull())
            assertEquals(emptyList(), templateDao.created)
        }
    }

    @Test
    fun `createTemplate rejects whitespace only message`() {
        runBlocking {
            val templateDao = FakeTemplateDao()
            val repository = repository(templateDao = templateDao)

            val result = repository.createTemplate(userId = USER_ID, message = "   ")

            assertIs<TemplateException.InvalidTemplateData>(result.exceptionOrNull())
            assertEquals(emptyList(), templateDao.created)
        }
    }

    @Test
    fun `getTemplates resolves current user to seeker profile and returns DAO order`() {
        runBlocking {
            val templates = listOf(
                TemplateEntity(id = 2, seekerId = SEEKER_ID, message = "Second"),
                TemplateEntity(id = 1, seekerId = SEEKER_ID, message = "First"),
            )
            val templateDao = FakeTemplateDao(templates = templates)
            val repository = repository(templateDao = templateDao)

            val result = repository.getTemplates(userId = USER_ID)

            assertTrue(result.isSuccess)
            assertEquals(listOf(2L, 1L), result.getOrThrow().map { it.id })
            assertEquals(SEEKER_ID, templateDao.lastGetTemplatesSeekerId)
        }
    }

    @Test
    fun `getTemplate returns owned template`() {
        runBlocking {
            val repository = repository()

            val result = repository.getTemplate(userId = USER_ID, id = TEMPLATE_ID)

            assertTrue(result.isSuccess)
            assertEquals(TEMPLATE_ID, result.getOrThrow().id)
            assertEquals(MESSAGE, result.getOrThrow().message)
        }
    }

    @Test
    fun `getTemplate returns TemplateNotFound when DAO returns null`() {
        runBlocking {
            val repository = repository(templateDao = FakeTemplateDao(template = null))

            val result = repository.getTemplate(userId = USER_ID, id = TEMPLATE_ID)

            assertIs<TemplateException.TemplateNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `updateTemplate changes owned message`() {
        runBlocking {
            val templateDao = FakeTemplateDao()
            val repository = repository(templateDao = templateDao)

            val result = repository.updateTemplate(userId = USER_ID, id = TEMPLATE_ID, message = UPDATED_MESSAGE)

            assertTrue(result.isSuccess)
            assertEquals(Triple(TEMPLATE_ID, SEEKER_ID, UPDATED_MESSAGE), templateDao.updated.single())
        }
    }

    @Test
    fun `updateTemplate rejects whitespace only message`() {
        runBlocking {
            val templateDao = FakeTemplateDao()
            val repository = repository(templateDao = templateDao)

            val result = repository.updateTemplate(userId = USER_ID, id = TEMPLATE_ID, message = "   ")

            assertIs<TemplateException.InvalidTemplateData>(result.exceptionOrNull())
            assertEquals(emptyList(), templateDao.updated)
        }
    }

    @Test
    fun `updateTemplate returns TemplateNotFound when template is missing or cross owner`() {
        runBlocking {
            val repository = repository(templateDao = FakeTemplateDao(updateResult = false))

            val result = repository.updateTemplate(userId = USER_ID, id = TEMPLATE_ID, message = UPDATED_MESSAGE)

            assertIs<TemplateException.TemplateNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `deleteTemplate removes owned template`() {
        runBlocking {
            val templateDao = FakeTemplateDao()
            val repository = repository(templateDao = templateDao)

            val result = repository.deleteTemplate(userId = USER_ID, id = TEMPLATE_ID)

            assertTrue(result.isSuccess)
            assertEquals(TEMPLATE_ID to SEEKER_ID, templateDao.deleted.single())
        }
    }

    @Test
    fun `deleteTemplate returns TemplateNotFound when template is missing or cross owner`() {
        runBlocking {
            val repository = repository(templateDao = FakeTemplateDao(deleteResult = false))

            val result = repository.deleteTemplate(userId = USER_ID, id = TEMPLATE_ID)

            assertIs<TemplateException.TemplateNotFound>(result.exceptionOrNull())
        }
    }

    private fun repository(
        templateDao: TemplateDao = FakeTemplateDao(),
        seekerProfileDao: SeekerProfileDao = FakeSeekerProfileDao(),
    ) = DefaultTemplateRepository(
        templateDao = templateDao,
        seekerProfileDao = seekerProfileDao,
    )

    private class FakeTemplateDao(
        private val template: TemplateEntity? = templateEntity(),
        private val templates: List<TemplateEntity> = listOf(templateEntity()),
        private val updateResult: Boolean = true,
        private val deleteResult: Boolean = true,
    ) : TemplateDao {
        val created = mutableListOf<Pair<Long, String>>()
        val updated = mutableListOf<Triple<Long, Long, String>>()
        val deleted = mutableListOf<Pair<Long, Long>>()
        var lastGetTemplatesSeekerId: Long? = null

        override suspend fun createTemplate(seekerId: Long, message: String): Long {
            created += seekerId to message
            return TEMPLATE_ID
        }

        override suspend fun getTemplates(seekerId: Long): List<TemplateEntity> {
            lastGetTemplatesSeekerId = seekerId
            return templates
        }

        override suspend fun getTemplate(id: Long, seekerId: Long): TemplateEntity? = template

        override suspend fun updateTemplate(id: Long, seekerId: Long, message: String): Boolean {
            updated += Triple(id, seekerId, message)
            return updateResult
        }

        override suspend fun deleteTemplate(id: Long, seekerId: Long): Boolean {
            deleted += id to seekerId
            return deleteResult
        }
    }

    private class FakeSeekerProfileDao(
        private val profile: SeekerProfileEntity? = seekerProfile(),
    ) : SeekerProfileDao {
        override suspend fun createProfile(profile: SeekerProfileEntity): Long = profile.id
        override suspend fun getProfile(id: Long): SeekerProfileEntity? = profile
        override suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity? = profile
        override suspend fun updateProfile(profile: SeekerProfileEntity): Boolean = true
        override suspend fun deleteProfile(id: Long): Boolean = true
        override suspend fun profileExists(userId: Long): Boolean = profile != null
        override suspend fun getProfileWithWorkExperienceByUserId(userId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? = profile?.let { it to emptyList() }
        override suspend fun getProfileWithWorkExperienceByProfileId(profileId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? = profile?.let { it to emptyList() }
    }

    private companion object {
        const val USER_ID = 7L
        const val SEEKER_ID = 11L
        const val TEMPLATE_ID = 13L
        const val MESSAGE = "Hello, I am interested in this role."
        const val UPDATED_MESSAGE = "Updated"

        fun seekerProfile() = SeekerProfileEntity(
            id = SEEKER_ID,
            userId = USER_ID,
            specialty = "Backend Developer",
            experienceYears = 5,
            desiredSalary = 5000,
            aboutMe = null,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
        )

        fun templateEntity() = TemplateEntity(
            id = TEMPLATE_ID,
            seekerId = SEEKER_ID,
            message = MESSAGE,
        )
    }
}
