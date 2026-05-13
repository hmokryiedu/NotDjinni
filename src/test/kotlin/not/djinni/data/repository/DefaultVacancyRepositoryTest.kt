package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.common.SortDirection
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancySortField
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.domain.exception.vacancy.VacancyException
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultVacancyRepositoryTest {

    @Test
    fun `getPublicVacancies forces active status when filter has no statuses`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao)

        val result = repository.getPublicVacancies(VacancyFilter(), limit = 7, offset = 3)

        assertTrue(result.isSuccess)
        assertEquals(listOf(VacancyStatusCode.ACTIVE), vacancyDao.lastDetailsFilter?.statuses)
        assertEquals(7, vacancyDao.lastDetailsLimit)
        assertEquals(3, vacancyDao.lastDetailsOffset)
    }

    @Test
    fun `getPublicVacancies overrides inactive status filters`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao)

        val result = repository.getPublicVacancies(
            VacancyFilter(statuses = listOf(VacancyStatusCode.PAUSED, VacancyStatusCode.CLOSED))
        )

        assertTrue(result.isSuccess)
        assertEquals(listOf(VacancyStatusCode.ACTIVE), vacancyDao.lastDetailsFilter?.statuses)
    }

    @Test
    fun `getPublicVacancies preserves filters sorting limit and offset`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao)
        val filter = VacancyFilter(
            companyId = 17L,
            categories = listOf(JobCategoryCode.SOFTWARE_DEV),
            statuses = listOf(VacancyStatusCode.DRAFT),
            employmentTypes = listOf(EmploymentTypeCode.FULL_TIME),
            salaryMin = 3000,
            salaryMax = 6000,
            experienceYears = 4,
            searchQuery = "Kotlin",
            sortBy = VacancySortField.SALARY_MAX,
            sortDirection = SortDirection.ASC,
        )

        val result = repository.getPublicVacancies(filter, limit = 12, offset = 24)

        assertTrue(result.isSuccess)
        assertEquals(
            filter.copy(statuses = listOf(VacancyStatusCode.ACTIVE)),
            vacancyDao.lastDetailsFilter
        )
        assertEquals(12, vacancyDao.lastDetailsLimit)
        assertEquals(24, vacancyDao.lastDetailsOffset)
    }

    @Test
    fun `getPublicCompanyVacancies forces company and active status`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao)

        val result = repository.getPublicCompanyVacancies(companyId = 31L, limit = 5, offset = 10)

        assertTrue(result.isSuccess)
        assertEquals(
            VacancyFilter(companyId = 31L, statuses = listOf(VacancyStatusCode.ACTIVE)),
            vacancyDao.lastDetailsFilter
        )
        assertEquals(5, vacancyDao.lastDetailsLimit)
        assertEquals(10, vacancyDao.lastDetailsOffset)
    }

    @Test
    fun `getPublicRecentVacancies forces active newest first and zero offset`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao)

        val result = repository.getPublicRecentVacancies(limit = 9)

        assertTrue(result.isSuccess)
        assertEquals(
            VacancyFilter(
                statuses = listOf(VacancyStatusCode.ACTIVE),
                sortBy = VacancySortField.CREATED_AT,
                sortDirection = SortDirection.DESC,
            ),
            vacancyDao.lastDetailsFilter
        )
        assertEquals(9, vacancyDao.lastDetailsLimit)
        assertEquals(0, vacancyDao.lastDetailsOffset)
    }

    @Test
    fun `getPublicVacancyWithDetails returns active vacancy`() = runBlocking {
        val repository = repository(FakeVacancyDao(detailStatus = VacancyStatusCode.ACTIVE))

        val result = repository.getPublicVacancyWithDetails(VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(VacancyStatusCode.ACTIVE, result.getOrThrow().status)
    }

    @Test
    fun `getPublicVacancyWithDetails returns not found for inactive vacancy`() = runBlocking {
        listOf(
            VacancyStatusCode.DRAFT,
            VacancyStatusCode.PAUSED,
            VacancyStatusCode.CLOSED,
            VacancyStatusCode.EXPIRED,
        ).forEach { inactiveStatus ->
            val repository = repository(FakeVacancyDao(detailStatus = inactiveStatus))

            val result = repository.getPublicVacancyWithDetails(VACANCY_ID)

            assertIs<VacancyException.VacancyNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `public vacancy detail keeps applications count`() = runBlocking {
        val repository = repository(FakeVacancyDao(applicationsCount = 4))

        val result = repository.getPublicVacancyWithDetails(VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(4, result.getOrThrow().applicationsCount)
    }

    private fun repository(vacancyDao: VacancyDao) = DefaultVacancyRepository(
        vacancyDao = vacancyDao,
        employerProfileDao = FakeEmployerProfileDao(),
    )

    private class FakeVacancyDao(
        private val detailStatus: VacancyStatusCode = VacancyStatusCode.ACTIVE,
        private val applicationsCount: Int = 0,
    ) : VacancyDao {
        var lastDetailsFilter: VacancyFilter? = null
            private set
        var lastDetailsLimit: Int? = null
            private set
        var lastDetailsOffset: Int? = null
            private set

        override suspend fun createVacancy(vacancy: VacancyEntity): Long = vacancy.id
        override suspend fun getVacancy(id: Long): VacancyEntity? = vacancyEntity()
        override suspend fun getVacancyWithDetails(id: Long): VacancyWithDetailsEntity? = vacancyWithDetails(
            status = detailStatus,
            applicationsCount = applicationsCount,
        )

        override suspend fun updateVacancy(vacancy: VacancyEntity): Boolean = true
        override suspend fun deleteVacancy(id: Long): Boolean = true
        override suspend fun getVacancies(filter: VacancyFilter, limit: Int, offset: Int): List<VacancyEntity> {
            return listOf(vacancyEntity())
        }

        override suspend fun getVacanciesWithDetails(
            filter: VacancyFilter,
            limit: Int,
            offset: Int,
        ): List<VacancyWithDetailsEntity> {
            lastDetailsFilter = filter
            lastDetailsLimit = limit
            lastDetailsOffset = offset
            return listOf(vacancyWithDetails())
        }

        override suspend fun countVacancies(filter: VacancyFilter): Int = 1
        override suspend fun getVacanciesByCompany(companyId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> {
            return listOf(vacancyWithDetails())
        }

        override suspend fun getRecentVacancies(limit: Int): List<VacancyEntity> = listOf(vacancyEntity())
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
        const val VACANCY_ID = 13L
        const val COMPANY_ID = 17L
        val NOW: Instant = Instant.parse("2026-05-04T00:00:00Z")

        fun vacancyEntity(
            status: VacancyStatusCode = VacancyStatusCode.ACTIVE,
            applicationsCount: Int = 0,
        ) = VacancyEntity(
            id = VACANCY_ID,
            companyId = COMPANY_ID,
            title = "Kotlin Backend Developer",
            description = "Build Ktor services",
            salaryMin = 4000,
            salaryMax = 6000,
            minExperienceYears = 3,
            employmentType = EmploymentTypeCode.FULL_TIME,
            category = JobCategoryCode.SOFTWARE_DEV,
            status = status,
            createdAt = NOW,
            updatedAt = NOW,
            applicationsCount = applicationsCount,
        )

        fun vacancyWithDetails(
            status: VacancyStatusCode = VacancyStatusCode.ACTIVE,
            applicationsCount: Int = 0,
        ) = VacancyWithDetailsEntity(
            vacancy = vacancyEntity(status = status, applicationsCount = applicationsCount),
            company = CompanyEntity(
                id = COMPANY_ID,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
            applicationsCount = applicationsCount,
        )

        fun employerProfile() = EmployerProfileWithCompany(
            profile = EmployerProfileEntity(id = 7L, userId = 11L, companyId = COMPANY_ID, role = "Owner"),
            company = CompanyEntity(
                id = COMPANY_ID,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
        )
    }
}
