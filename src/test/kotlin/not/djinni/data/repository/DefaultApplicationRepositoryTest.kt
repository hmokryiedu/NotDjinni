package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.application.ApplicationDao
import not.djinni.database.api.application.ApplicationEntity
import not.djinni.database.api.application.ApplicationFilter
import not.djinni.database.api.application.ApplicationWithDetailsEntity
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.domain.exception.application.ApplicationException
import not.djinni.model.application.ApplicationStatusCode
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultApplicationRepositoryTest {

    @Test
    fun `getMyApplicationByVacancy returns current seeker application for vacancy`() = runBlocking {
        val applicationDao = FakeApplicationDao()
        val repository = repository(applicationDao = applicationDao)

        val result = repository.getMyApplicationByVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(APPLICATION_ID, result.getOrThrow().id)
        assertEquals(VACANCY_ID, applicationDao.applicationQueries.single().vacancyId)
        assertEquals(SEEKER_ID, applicationDao.applicationQueries.single().jobSeekerId)
    }

    @Test
    fun `getMyApplicationByVacancy returns seeker profile not found when current user has no seeker profile`() = runBlocking {
        val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

        val result = repository.getMyApplicationByVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertIs<ApplicationException.SeekerProfileNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `getMyApplicationByVacancy returns application not found when current seeker has no application for vacancy`() = runBlocking {
        val repository = repository(applicationDao = FakeApplicationDao(applications = emptyList()))

        val result = repository.getMyApplicationByVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertIs<ApplicationException.ApplicationNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `withdrawApplication updates active statuses to withdrawn`() = runBlocking {
        listOf(
            ApplicationStatusCode.APPLIED,
            ApplicationStatusCode.REVIEWING,
            ApplicationStatusCode.INTERVIEW,
            ApplicationStatusCode.TEST_TASK,
            ApplicationStatusCode.OFFER,
        ).forEach { status ->
            val applicationDao = FakeApplicationDao(status = status)
            val repository = repository(applicationDao = applicationDao)

            val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

            assertTrue(result.isSuccess)
            assertEquals(APPLICATION_ID to ApplicationStatusCode.WITHDRAWN, applicationDao.statusUpdates.single())
        }
    }

    @Test
    fun `withdrawApplication succeeds for withdrawn application without status update`() = runBlocking {
        val applicationDao = FakeApplicationDao(status = ApplicationStatusCode.WITHDRAWN)
        val repository = repository(applicationDao = applicationDao)

        val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), applicationDao.statusUpdates)
    }

    @Test
    fun `withdrawApplication rejects hired application`() = runBlocking {
        val repository = repository(applicationDao = FakeApplicationDao(status = ApplicationStatusCode.HIRED))

        val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

        assertIs<ApplicationException.InvalidApplicationData>(result.exceptionOrNull())
    }

    @Test
    fun `withdrawApplication rejects rejected application`() = runBlocking {
        val repository = repository(applicationDao = FakeApplicationDao(status = ApplicationStatusCode.REJECTED))

        val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

        assertIs<ApplicationException.InvalidApplicationData>(result.exceptionOrNull())
    }

    @Test
    fun `withdrawApplication returns seeker profile not found when current user has no seeker profile`() = runBlocking {
        val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

        val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

        assertIs<ApplicationException.SeekerProfileNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `withdrawApplication returns application not found when application is missing`() = runBlocking {
        val repository = repository(applicationDao = FakeApplicationDao(application = null))

        val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

        assertIs<ApplicationException.ApplicationNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `withdrawApplication returns unauthorized when application belongs to another seeker`() = runBlocking {
        val repository = repository(applicationDao = FakeApplicationDao(jobSeekerId = OTHER_SEEKER_ID))

        val result = repository.withdrawApplication(userId = USER_ID, id = APPLICATION_ID)

        assertIs<ApplicationException.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `getVacancyApplications keeps withdrawn applications and does not set status filter`() = runBlocking {
        val withdrawnApplication = applicationWithDetails(status = ApplicationStatusCode.WITHDRAWN)
        val activeApplication = applicationWithDetails(status = ApplicationStatusCode.APPLIED)
        val applicationDao = FakeApplicationDao(applications = listOf(withdrawnApplication, activeApplication))
        val repository = repository(applicationDao = applicationDao)

        val result = repository.getVacancyApplications(
            userId = USER_ID,
            vacancyId = VACANCY_ID,
            limit = 20,
            offset = 0
        )

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(ApplicationStatusCode.WITHDRAWN, ApplicationStatusCode.APPLIED),
            result.getOrThrow().map { it.statusCode }
        )
        val filter = applicationDao.applicationQueries.single()
        assertEquals(VACANCY_ID, filter.vacancyId)
        assertEquals(null, filter.statusCode)
    }

    private fun repository(
        applicationDao: ApplicationDao = FakeApplicationDao(),
        seekerProfileDao: SeekerProfileDao = FakeSeekerProfileDao(),
    ) = DefaultApplicationRepository(
        applicationDao = applicationDao,
        seekerProfileDao = seekerProfileDao,
        vacancyDao = FakeVacancyDao(),
        employerProfileDao = FakeEmployerProfileDao(),
    )

    private class FakeApplicationDao(
        status: ApplicationStatusCode = ApplicationStatusCode.APPLIED,
        jobSeekerId: Long = SEEKER_ID,
        private val application: ApplicationWithDetailsEntity? = applicationWithDetails(
            status = status,
            jobSeekerId = jobSeekerId,
        ),
        private val applications: List<ApplicationWithDetailsEntity> = listOf(applicationWithDetails()),
    ) : ApplicationDao {
        val statusUpdates = mutableListOf<Pair<Long, ApplicationStatusCode>>()
        val applicationQueries = mutableListOf<ApplicationFilter>()

        override suspend fun createApplication(application: ApplicationEntity): Long = application.id
        override suspend fun getApplication(id: Long): ApplicationWithDetailsEntity? = application
        override suspend fun updateApplication(application: ApplicationEntity): Boolean = true

        override suspend fun updateApplicationStatus(id: Long, statusCode: ApplicationStatusCode): Boolean {
            statusUpdates += id to statusCode
            return true
        }

        override suspend fun deleteApplication(id: Long): Boolean = true
        override suspend fun getApplications(
            filter: ApplicationFilter,
            limit: Int,
            offset: Int
        ): List<ApplicationWithDetailsEntity> {
            applicationQueries += filter
            return applications
        }

        override suspend fun countApplications(filter: ApplicationFilter): Int = 0
        override suspend fun hasApplied(vacancyId: Long, jobSeekerId: Long): Boolean = false
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

    private class FakeVacancyDao : VacancyDao {
        override suspend fun createVacancy(vacancy: VacancyEntity): Long = vacancy.id
        override suspend fun getVacancy(id: Long): VacancyEntity? = vacancyEntity()
        override suspend fun getVacancyWithDetails(id: Long): VacancyWithDetailsEntity? = vacancyWithDetails()
        override suspend fun updateVacancy(vacancy: VacancyEntity): Boolean = true
        override suspend fun deleteVacancy(id: Long): Boolean = true
        override suspend fun getVacancies(filter: VacancyFilter, limit: Int, offset: Int): List<VacancyEntity> = emptyList()
        override suspend fun getVacanciesWithDetails(filter: VacancyFilter, limit: Int, offset: Int): List<VacancyWithDetailsEntity> = emptyList()
        override suspend fun getAppliedVacancies(
            jobSeekerId: Long,
            limit: Int,
            offset: Int,
            applicationStatuses: List<ApplicationStatusCode>,
        ): List<VacancyWithDetailsEntity> = emptyList()
        override suspend fun countVacancies(filter: VacancyFilter): Int = 0
        override suspend fun getVacanciesByCompany(companyId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> = emptyList()
        override suspend fun getRecentVacancies(limit: Int): List<VacancyEntity> = emptyList()
        override suspend fun updateVacancyStatus(id: Long, status: VacancyStatusCode): Boolean = true
        override suspend fun vacancyExists(id: Long): Boolean = true
    }

    private class FakeEmployerProfileDao : EmployerProfileDao {
        override suspend fun createProfile(profile: EmployerProfileEntity): Long = profile.id
        override suspend fun getProfile(id: Long): EmployerProfileWithCompany? = employerProfile()
        override suspend fun getProfileByUserId(userId: Long): EmployerProfileWithCompany? = employerProfile()
        override suspend fun updateProfile(profile: EmployerProfileEntity): Boolean = true
        override suspend fun deleteProfile(id: Long): Boolean = true
        override suspend fun profileExists(userId: Long): Boolean = true
    }

    private companion object {
        const val USER_ID = 7L
        const val SEEKER_ID = 11L
        const val OTHER_SEEKER_ID = 12L
        const val APPLICATION_ID = 13L
        const val VACANCY_ID = 17L
        const val COMPANY_ID = 19L
        val NOW: Instant = Instant.parse("2026-05-04T00:00:00Z")

        fun applicationWithDetails(
            status: ApplicationStatusCode = ApplicationStatusCode.APPLIED,
            jobSeekerId: Long = SEEKER_ID,
        ) = ApplicationWithDetailsEntity(
            application = ApplicationEntity(
                id = APPLICATION_ID,
                vacancyId = VACANCY_ID,
                jobSeekerId = jobSeekerId,
                statusCode = status,
                coverLetter = "I build Kotlin services",
                createdAt = NOW,
                updatedAt = NOW,
            ),
            vacancy = vacancyWithDetails(),
            jobSeeker = seekerProfile(id = jobSeekerId),
        )

        fun seekerProfile(id: Long = SEEKER_ID) = SeekerProfileEntity(
            id = id,
            userId = USER_ID,
            specialty = "Backend Developer",
            experienceYears = 5,
            desiredSalary = 5000,
            aboutMe = null,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
        )

        fun vacancyEntity() = VacancyEntity(
            id = VACANCY_ID,
            companyId = COMPANY_ID,
            title = "Kotlin Backend Developer",
            description = "Build Ktor services",
            salaryMin = 4000,
            salaryMax = 6000,
            minExperienceYears = 3,
            employmentType = EmploymentTypeCode.FULL_TIME,
            category = JobCategoryCode.SOFTWARE_DEV,
            status = VacancyStatusCode.ACTIVE,
            createdAt = NOW,
            updatedAt = NOW,
        )

        fun vacancyWithDetails() = VacancyWithDetailsEntity(
            vacancy = vacancyEntity(),
            company = CompanyEntity(
                id = COMPANY_ID,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
        )

        fun employerProfile() = EmployerProfileWithCompany(
            profile = EmployerProfileEntity(id = 23L, userId = 29L, companyId = COMPANY_ID, role = "Owner"),
            company = CompanyEntity(
                id = COMPANY_ID,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
        )
    }
}
