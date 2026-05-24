package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.common.SortDirection
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancySortField
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.domain.exception.vacancy.VacancyException
import not.djinni.model.application.ApplicationStatusCode
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

    @Test
    fun `public vacancy detail keeps views count`() = runBlocking {
        val repository = repository(FakeVacancyDao(viewsCount = 6))

        val result = repository.getPublicVacancyWithDetails(VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(6, result.getOrThrow().viewsCount)
    }

    @Test
    fun `public vacancy list keeps views count`() = runBlocking {
        val repository = repository(FakeVacancyDao(viewsCount = 8))

        val result = repository.getPublicVacancies(VacancyFilter(), limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        assertEquals(8, result.getOrThrow().single().viewsCount)
    }

    @Test
    fun `recommended vacancy source keeps views count`() = runBlocking {
        val repository = repository(FakeVacancyDao(viewsCount = 10))

        val result = repository.getVacancies(VacancyFilter(), limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrThrow().single().viewsCount)
    }

    @Test
    fun `getPublicVacanciesForSeeker marks favorite state in bulk`() = runBlocking {
        val favoriteDao = FakeFavoriteVacancyDao(favoriteIds = setOf(VACANCY_ID))
        val repository = repository(FakeVacancyDao(), favoriteDao = favoriteDao)

        val result = repository.getPublicVacanciesForSeeker(USER_ID, VacancyFilter(), limit = 7, offset = 3)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrThrow().single().isFavorite)
        assertEquals(SEEKER_ID to setOf(VACANCY_ID), favoriteDao.lastFavoriteIdsRequest)
    }

    @Test
    fun `getPublicVacancyWithDetailsForSeeker marks missing favorite as false`() = runBlocking {
        val repository = repository(FakeVacancyDao(), favoriteDao = FakeFavoriteVacancyDao(favoriteIds = emptySet()))

        val result = repository.getPublicVacancyWithDetailsForSeeker(USER_ID, VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrThrow().isFavorite)
    }

    @Test
    fun `getAppliedVacancies resolves seeker profile before querying DAO`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(
            vacancyDao,
            seekerProfileDao = FakeSeekerProfileDao(onGetByUserId = { vacancyDao.lastAppliedUserIdSeenBySeekerDao = it })
        )

        val result = repository.getAppliedVacancies(USER_ID, limit = 5, offset = 10)

        assertTrue(result.isSuccess)
        assertEquals(USER_ID, vacancyDao.lastAppliedUserIdSeenBySeekerDao)
        assertEquals(SEEKER_ID, vacancyDao.lastAppliedJobSeekerId)
        assertEquals(5, vacancyDao.lastAppliedLimit)
        assertEquals(10, vacancyDao.lastAppliedOffset)
        assertEquals(emptyList(), vacancyDao.lastAppliedApplicationStatuses)
    }

    @Test
    fun `getAppliedVacancies returns unauthorized when seeker profile is missing`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao, seekerProfileDao = FakeSeekerProfileDao(profile = null))

        val result = repository.getAppliedVacancies(USER_ID, limit = 5, offset = 10)

        assertIs<VacancyException.Unauthorized>(result.exceptionOrNull())
        assertEquals(null, vacancyDao.lastAppliedJobSeekerId)
    }

    @Test
    fun `getAppliedVacancies preserves DAO order and marks favorites in bulk`() = runBlocking {
        val vacancyDao = FakeVacancyDao(
            appliedVacancies = listOf(
                vacancyWithDetails(vacancyId = 31L, viewsCount = 12),
                vacancyWithDetails(vacancyId = 29L, viewsCount = 14),
            )
        )
        val favoriteDao = FakeFavoriteVacancyDao(favoriteIds = setOf(29L))
        val repository = repository(vacancyDao, favoriteDao = favoriteDao)

        val result = repository.getAppliedVacancies(USER_ID, limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        assertEquals(listOf(31L, 29L), result.getOrThrow().map { it.id })
        assertEquals(listOf(false, true), result.getOrThrow().map { it.isFavorite })
        assertEquals(listOf(12, 14), result.getOrThrow().map { it.viewsCount })
        assertEquals(SEEKER_ID to setOf(31L, 29L), favoriteDao.lastFavoriteIdsRequest)
    }

    @Test
    fun `getAppliedVacancies passes application statuses to DAO with limit and offset`() = runBlocking {
        val vacancyDao = FakeVacancyDao()
        val repository = repository(vacancyDao)
        val statuses = listOf(ApplicationStatusCode.APPLIED, ApplicationStatusCode.REVIEWING)

        val result = repository.getAppliedVacancies(
            userId = USER_ID,
            limit = 9,
            offset = 4,
            applicationStatuses = statuses,
        )

        assertTrue(result.isSuccess)
        assertEquals(SEEKER_ID, vacancyDao.lastAppliedJobSeekerId)
        assertEquals(9, vacancyDao.lastAppliedLimit)
        assertEquals(4, vacancyDao.lastAppliedOffset)
        assertEquals(statuses, vacancyDao.lastAppliedApplicationStatuses)
    }

    @Test
    fun `company employer and recent vacancy paths keep views count`() = runBlocking {
        val repository = repository(FakeVacancyDao(viewsCount = 16))

        val employerResult = repository.getEmployerVacancies(USER_ID, limit = 20, offset = 0)
        val companyResult = repository.getCompanyVacancies(COMPANY_ID, limit = 20, offset = 0)
        val recentResult = repository.getRecentVacancies(limit = 10)

        assertTrue(employerResult.isSuccess)
        assertTrue(companyResult.isSuccess)
        assertTrue(recentResult.isSuccess)
        assertEquals(16, employerResult.getOrThrow().single().viewsCount)
        assertEquals(16, companyResult.getOrThrow().single().viewsCount)
        assertEquals(16, recentResult.getOrThrow().single().viewsCount)
    }

    @Test
    fun `guest public vacancy detail leaves favorite state absent`() = runBlocking {
        val repository = repository(FakeVacancyDao())

        val result = repository.getPublicVacancyWithDetails(VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrThrow().isFavorite)
    }

    private fun repository(
        vacancyDao: VacancyDao,
        favoriteDao: FavoriteVacancyDao = FakeFavoriteVacancyDao(),
        seekerProfileDao: SeekerProfileDao = FakeSeekerProfileDao(),
    ) = DefaultVacancyRepository(
        vacancyDao = vacancyDao,
        employerProfileDao = FakeEmployerProfileDao(),
        seekerProfileDao = seekerProfileDao,
        favoriteVacancyDao = favoriteDao,
    )

    private class FakeFavoriteVacancyDao(
        private val favoriteIds: Set<Long> = emptySet(),
    ) : FavoriteVacancyDao {
        var lastFavoriteIdsRequest: Pair<Long, Set<Long>>? = null
            private set

        override suspend fun addFavoriteVacancy(favorite: FavoriteVacancyEntity): Boolean = true
        override suspend fun removeFavoriteVacancy(vacancyId: Long, jobSeekerId: Long): Boolean = true
        override suspend fun getFavoriteVacancies(jobSeekerId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> = emptyList()
        override suspend fun favoriteExists(vacancyId: Long, jobSeekerId: Long): Boolean = favoriteIds.contains(vacancyId)

        override suspend fun getFavoriteVacancyIds(jobSeekerId: Long, vacancyIds: Set<Long>): Set<Long> {
            lastFavoriteIdsRequest = jobSeekerId to vacancyIds
            return favoriteIds.intersect(vacancyIds)
        }
    }

    private class FakeSeekerProfileDao(
        private val profile: SeekerProfileEntity? = seekerProfile(),
        private val onGetByUserId: ((Long) -> Unit)? = null,
    ) : SeekerProfileDao {
        override suspend fun createProfile(profile: SeekerProfileEntity): Long = profile.id
        override suspend fun getProfile(id: Long): SeekerProfileEntity? = profile
        override suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity? {
            onGetByUserId?.invoke(userId)
            return profile
        }
        override suspend fun updateProfile(profile: SeekerProfileEntity): Boolean = true
        override suspend fun deleteProfile(id: Long): Boolean = true
        override suspend fun profileExists(userId: Long): Boolean = profile != null
        override suspend fun getProfileWithWorkExperienceByUserId(userId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? = profile?.let { it to emptyList() }
        override suspend fun getProfileWithWorkExperienceByProfileId(profileId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>? = profile?.let { it to emptyList() }
    }

    private class FakeVacancyDao(
        private val detailStatus: VacancyStatusCode = VacancyStatusCode.ACTIVE,
        private val applicationsCount: Int = 0,
        private val viewsCount: Int = 0,
        private val appliedVacancies: List<VacancyWithDetailsEntity> = listOf(vacancyWithDetails()),
    ) : VacancyDao {
        var lastDetailsFilter: VacancyFilter? = null
            private set
        var lastDetailsLimit: Int? = null
            private set
        var lastDetailsOffset: Int? = null
            private set
        var lastAppliedJobSeekerId: Long? = null
            private set
        var lastAppliedLimit: Int? = null
            private set
        var lastAppliedOffset: Int? = null
            private set
        var lastAppliedApplicationStatuses: List<ApplicationStatusCode>? = null
            private set
        var lastAppliedUserIdSeenBySeekerDao: Long? = null

        override suspend fun createVacancy(vacancy: VacancyEntity): Long = vacancy.id
        override suspend fun getVacancy(id: Long): VacancyEntity? = vacancyEntity()
        override suspend fun getVacancyWithDetails(id: Long): VacancyWithDetailsEntity? = vacancyWithDetails(
            status = detailStatus,
            applicationsCount = applicationsCount,
            viewsCount = viewsCount,
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
            return listOf(vacancyWithDetails(viewsCount = viewsCount))
        }

        override suspend fun getAppliedVacancies(
            jobSeekerId: Long,
            limit: Int,
            offset: Int,
            applicationStatuses: List<ApplicationStatusCode>,
        ): List<VacancyWithDetailsEntity> {
            lastAppliedJobSeekerId = jobSeekerId
            lastAppliedLimit = limit
            lastAppliedOffset = offset
            lastAppliedApplicationStatuses = applicationStatuses
            return appliedVacancies
        }

        override suspend fun countVacancies(filter: VacancyFilter): Int = 1
        override suspend fun getVacanciesByCompany(companyId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> {
            return listOf(vacancyWithDetails(viewsCount = viewsCount))
        }

        override suspend fun getRecentVacancies(limit: Int): List<VacancyEntity> = listOf(vacancyEntity(viewsCount = viewsCount))
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
        const val USER_ID = 19L
        const val SEEKER_ID = 23L
        val NOW: Instant = Instant.parse("2026-05-04T00:00:00Z")

        fun vacancyEntity(
            vacancyId: Long = VACANCY_ID,
            status: VacancyStatusCode = VacancyStatusCode.ACTIVE,
            applicationsCount: Int = 0,
            viewsCount: Int = 0,
        ) = VacancyEntity(
            id = vacancyId,
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
            viewsCount = viewsCount,
        )

        fun vacancyWithDetails(
            vacancyId: Long = VACANCY_ID,
            status: VacancyStatusCode = VacancyStatusCode.ACTIVE,
            applicationsCount: Int = 0,
            viewsCount: Int = 0,
        ) = VacancyWithDetailsEntity(
            vacancy = vacancyEntity(
                vacancyId = vacancyId,
                status = status,
                applicationsCount = applicationsCount,
                viewsCount = viewsCount,
            ),
            company = CompanyEntity(
                id = COMPANY_ID,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
            applicationsCount = applicationsCount,
            viewsCount = viewsCount,
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

        fun seekerProfile() = SeekerProfileEntity(
            id = SEEKER_ID,
            userId = USER_ID,
            specialty = "Backend Developer",
            experienceYears = 5,
            desiredSalary = 5000,
            aboutMe = null,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
        )
    }
}
