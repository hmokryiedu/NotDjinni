package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceDao
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.domain.exception.seeker.SeekerProfileException
import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience
import not.djinni.model.vacancy.JobCategoryCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultSeekerProfileRepositoryTest {

    @Test
    fun `updateProfile updates scalar fields returns updated profile and preserves work experience`() = runBlocking {
        val workExperienceDao = FakeWorkExperienceDao()
        val seekerProfileDao = FakeSeekerProfileDao { workExperienceDao.currentForProfile(PROFILE_ID) }
        val repository = repository(seekerProfileDao, workExperienceDao)
        val update = seekerProfile(
            id = 0,
            speciality = "Backend Kotlin",
            experienceYears = 6,
            desiredSalary = 7000,
            aboutMe = null,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
            workExperience = emptyList(),
        )

        val result = repository.updateProfile(USER_ID, update)

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals(PROFILE_ID, updated.id)
        assertEquals("Backend Kotlin", updated.speciality)
        assertEquals(6, updated.experienceYears)
        assertEquals(7000, updated.desiredSalary)
        assertEquals(null, updated.aboutMe)
        assertEquals(listOf(EXPERIENCE_ID), updated.workExperience.map { it.id })
        assertEquals(emptyList(), workExperienceDao.deletedProfileIds)
    }

    @Test
    fun `updateProfile returns ProfileNotFound when current seeker profile is missing`() = runBlocking {
        val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

        val result = repository.updateProfile(USER_ID, seekerProfile())

        assertIs<SeekerProfileException.ProfileNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `updateProfile maps invalid scalar data to InvalidProfileData`() = runBlocking {
        val repository = repository()

        val result = repository.updateProfile(USER_ID, seekerProfile(speciality = " "))

        assertIs<SeekerProfileException.InvalidProfileData>(result.exceptionOrNull())
    }

    @Test
    fun `addWorkExperience creates owned experience and returns updated profile`() = runBlocking {
        val workExperienceDao = FakeWorkExperienceDao()
        val repository = repository(
            seekerProfileDao = FakeSeekerProfileDao { workExperienceDao.currentForProfile(PROFILE_ID) },
            workExperienceDao = workExperienceDao,
        )

        val result = repository.addWorkExperience(USER_ID, workExperience(id = 0, companyName = "NewCo"))

        assertTrue(result.isSuccess)
        assertEquals(PROFILE_ID, workExperienceDao.created.single().profileId)
        assertEquals(listOf(EXPERIENCE_ID, CREATED_EXPERIENCE_ID), result.getOrThrow().workExperience.map { it.id })
    }

    @Test
    fun `updateWorkExperience updates owned experience and returns updated profile`() = runBlocking {
        val workExperienceDao = FakeWorkExperienceDao()
        val repository = repository(
            seekerProfileDao = FakeSeekerProfileDao { workExperienceDao.currentForProfile(PROFILE_ID) },
            workExperienceDao = workExperienceDao,
        )

        val result = repository.updateWorkExperience(
            userId = USER_ID,
            experienceId = EXPERIENCE_ID,
            experience = workExperience(id = 0, position = "Lead Engineer")
        )

        assertTrue(result.isSuccess)
        assertEquals("Lead Engineer", workExperienceDao.updated.single().position)
        assertEquals("Lead Engineer", result.getOrThrow().workExperience.single { it.id == EXPERIENCE_ID }.position)
    }

    @Test
    fun `deleteWorkExperience deletes owned experience and returns updated profile`() = runBlocking {
        val workExperienceDao = FakeWorkExperienceDao()
        val repository = repository(
            seekerProfileDao = FakeSeekerProfileDao { workExperienceDao.currentForProfile(PROFILE_ID) },
            workExperienceDao = workExperienceDao,
        )

        val result = repository.deleteWorkExperience(USER_ID, EXPERIENCE_ID)

        assertTrue(result.isSuccess)
        assertEquals(listOf(EXPERIENCE_ID), workExperienceDao.deletedIds)
        assertEquals(emptyList(), result.getOrThrow().workExperience.map { it.id })
    }

    @Test
    fun `updateWorkExperience returns Unauthorized for another seeker experience`() = runBlocking {
        val repository = repository(workExperienceDao = FakeWorkExperienceDao(ownerProfileId = OTHER_PROFILE_ID))

        val result = repository.updateWorkExperience(USER_ID, EXPERIENCE_ID, workExperience(id = 0))

        assertIs<SeekerProfileException.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `deleteWorkExperience returns Unauthorized for another seeker experience`() = runBlocking {
        val repository = repository(workExperienceDao = FakeWorkExperienceDao(ownerProfileId = OTHER_PROFILE_ID))

        val result = repository.deleteWorkExperience(USER_ID, EXPERIENCE_ID)

        assertIs<SeekerProfileException.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `updateWorkExperience returns WorkExperienceNotFound when experience is missing`() = runBlocking {
        val repository = repository(workExperienceDao = FakeWorkExperienceDao(existingExperience = null))

        val result = repository.updateWorkExperience(USER_ID, EXPERIENCE_ID, workExperience(id = 0))

        assertIs<SeekerProfileException.WorkExperienceNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `addWorkExperience maps blank company name to InvalidProfileData`() = runBlocking {
        val repository = repository()

        val result = repository.addWorkExperience(USER_ID, workExperience(id = 0, companyName = " "))

        assertIs<SeekerProfileException.InvalidProfileData>(result.exceptionOrNull())
    }

    @Test
    fun `addWorkExperience maps blank position to InvalidProfileData`() = runBlocking {
        val repository = repository()

        val result = repository.addWorkExperience(USER_ID, workExperience(id = 0, position = " "))

        assertIs<SeekerProfileException.InvalidProfileData>(result.exceptionOrNull())
    }

    @Test
    fun `addWorkExperience maps start date after end date to InvalidProfileData`() = runBlocking {
        val repository = repository()

        val result = repository.addWorkExperience(
            USER_ID,
            workExperience(id = 0, startDate = Instant.parse("2024-02-01T00:00:00Z"), endDate = Instant.parse("2024-01-01T00:00:00Z"))
        )

        assertIs<SeekerProfileException.InvalidProfileData>(result.exceptionOrNull())
    }

    @Test
    fun `addWorkExperience maps future start date to InvalidProfileData`() = runBlocking {
        val repository = repository()

        val result = repository.addWorkExperience(
            USER_ID,
            workExperience(id = 0, startDate = Instant.parse("2999-01-01T00:00:00Z"), endDate = null)
        )

        assertIs<SeekerProfileException.InvalidProfileData>(result.exceptionOrNull())
    }

    private fun repository(
        seekerProfileDao: SeekerProfileDao = FakeSeekerProfileDao(),
        workExperienceDao: WorkExperienceDao = FakeWorkExperienceDao(),
    ) = DefaultSeekerProfileRepository(seekerProfileDao, workExperienceDao)

    private class FakeSeekerProfileDao(
        private var profile: SeekerProfileEntity? = seekerProfileEntity(),
        private val workExperiences: () -> List<WorkExperienceEntity> = { listOf(workExperienceEntity()) },
    ) : SeekerProfileDao {
        override suspend fun createProfile(profile: SeekerProfileEntity): Long = PROFILE_ID
        override suspend fun getProfile(id: Long): SeekerProfileEntity? = profile?.takeIf { it.id == id }
        override suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity? = profile?.takeIf { it.userId == userId }

        override suspend fun updateProfile(profile: SeekerProfileEntity): Boolean {
            validateProfile(profile)
            this.profile = profile
            return true
        }

        override suspend fun deleteProfile(id: Long): Boolean = true
        override suspend fun profileExists(userId: Long): Boolean = profile != null

        override suspend fun getProfileWithWorkExperienceByUserId(userId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? {
            return profile?.takeIf { it.userId == userId }?.let { it to workExperiences() }
        }

        override suspend fun getProfileWithWorkExperienceByProfileId(profileId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? {
            return profile?.takeIf { it.id == profileId }?.let { it to workExperiences() }
        }

        private fun validateProfile(profile: SeekerProfileEntity) {
            when {
                profile.experienceYears < 0 -> error("Experience years cannot be negative")
                profile.specialty.isBlank() -> error("Specialty cannot be blank")
            }
        }
    }

    private class FakeWorkExperienceDao(
        private val ownerProfileId: Long = PROFILE_ID,
        private var existingExperience: WorkExperienceEntity? = workExperienceEntity(profileId = ownerProfileId),
    ) : WorkExperienceDao {
        val created = mutableListOf<WorkExperienceEntity>()
        val updated = mutableListOf<WorkExperienceEntity>()
        val deletedIds = mutableListOf<Long>()
        val deletedProfileIds = mutableListOf<Long>()

        override suspend fun createWorkExperience(experience: WorkExperienceEntity): Long {
            validateExperience(experience)
            created += experience.copy(id = CREATED_EXPERIENCE_ID)
            return CREATED_EXPERIENCE_ID
        }

        override suspend fun createWorkExperiences(experiences: List<WorkExperienceEntity>): List<Long> {
            experiences.forEach { validateExperience(it) }
            created += experiences
            return experiences.map { it.id }
        }

        override suspend fun getWorkExperience(id: Long): WorkExperienceEntity? = existingExperience?.takeIf { it.id == id }

        override suspend fun getWorkExperiencesByProfileId(profileId: Long): List<WorkExperienceEntity> {
            return listOfNotNull(existingExperience?.takeIf { it.profileId == profileId }) + created.filter { it.profileId == profileId }
        }

        override suspend fun updateWorkExperience(experience: WorkExperienceEntity): Boolean {
            validateExperience(experience)
            updated += experience
            existingExperience = experience
            return true
        }

        override suspend fun deleteWorkExperience(id: Long): Boolean {
            deletedIds += id
            existingExperience = null
            return true
        }

        override suspend fun deleteAllWorkExperiencesByProfileId(profileId: Long) {
            deletedProfileIds += profileId
            existingExperience = null
        }

        fun currentForProfile(profileId: Long): List<WorkExperienceEntity> {
            return listOfNotNull(existingExperience?.takeIf { it.profileId == profileId }) + created.filter {
                it.profileId == profileId
            }
        }

        private fun validateExperience(experience: WorkExperienceEntity) {
            when {
                experience.companyName.isBlank() -> error("Company name cannot be blank")
                experience.position.isBlank() -> error("Position cannot be blank")
                experience.endDate != null && experience.startDate > experience.endDate -> error("Start date cannot be after end date")
                experience.startDate >= Instant.parse("2026-05-13T00:00:00Z") -> error("Start date cannot be in the future")
            }
        }
    }

    private companion object {
        const val USER_ID = 10L
        const val PROFILE_ID = 20L
        const val OTHER_PROFILE_ID = 21L
        const val EXPERIENCE_ID = 30L
        const val CREATED_EXPERIENCE_ID = 31L

        fun seekerProfile(
            id: Long = PROFILE_ID,
            speciality: String = "Kotlin",
            experienceYears: Int = 5,
            desiredSalary: Int = 6000,
            aboutMe: String? = "About",
            jobCategory: JobCategoryCode = JobCategoryCode.SOFTWARE_DEV,
            workExperience: List<WorkExperience> = listOf(workExperience()),
        ) = SeekerProfile(
            id = id,
            speciality = speciality,
            experienceYears = experienceYears,
            desiredSalary = desiredSalary,
            aboutMe = aboutMe,
            jobCategory = jobCategory,
            workExperience = workExperience,
        )

        fun seekerProfileEntity(
            id: Long = PROFILE_ID,
            userId: Long = USER_ID,
            specialty: String = "Kotlin",
            experienceYears: Int = 5,
            desiredSalary: Int = 6000,
            aboutMe: String? = "About",
            jobCategory: JobCategoryCode = JobCategoryCode.SOFTWARE_DEV,
        ) = SeekerProfileEntity(
            id = id,
            userId = userId,
            specialty = specialty,
            experienceYears = experienceYears,
            desiredSalary = desiredSalary,
            aboutMe = aboutMe,
            jobCategory = jobCategory,
        )

        fun workExperience(
            id: Long = EXPERIENCE_ID,
            companyName: String = "Acme",
            position: String = "Engineer",
            description: String? = "Build",
            startDate: Instant = Instant.parse("2020-01-01T00:00:00Z"),
            endDate: Instant? = null,
        ) = WorkExperience(
            id = id,
            companyName = companyName,
            position = position,
            description = description,
            startDate = startDate,
            endDate = endDate,
        )

        fun workExperienceEntity(
            id: Long = EXPERIENCE_ID,
            profileId: Long = PROFILE_ID,
            companyName: String = "Acme",
            position: String = "Engineer",
            description: String? = "Build",
            startDate: Instant = Instant.parse("2020-01-01T00:00:00Z"),
            endDate: Instant? = null,
        ) = WorkExperienceEntity(
            id = id,
            profileId = profileId,
            companyName = companyName,
            position = position,
            description = description,
            startDate = startDate,
            endDate = endDate,
        )
    }
}
