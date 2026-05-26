package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.database.api.viewed.ViewedVacancyDao
import not.djinni.database.api.viewed.ViewedVacancyEntity
import not.djinni.database.api.viewed.ViewedVacancyWithDetailsEntity
import not.djinni.domain.exception.viewed.ViewedVacancyException
import not.djinni.model.application.ApplicationStatusCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultViewedVacancyRepositoryTest {

    @Test
    fun `trackViewedVacancy stores seeker view when vacancy is active`() = runBlocking {
        val viewedDao = FakeViewedVacancyDao()
        val repository = repository(viewedDao = viewedDao)

        val result = repository.trackViewedVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(VACANCY_ID, viewedDao.tracked.single().vacancyId)
        assertEquals(SEEKER_ID, viewedDao.tracked.single().jobSeekerId)
    }

    @Test
    fun `trackViewedVacancy updates existing seeker view through dao instead of duplicate`() = runBlocking {
        val viewedDao = FakeViewedVacancyDao()
        val repository = repository(viewedDao = viewedDao)

        repository.trackViewedVacancy(userId = USER_ID, vacancyId = VACANCY_ID)
        val result = repository.trackViewedVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertTrue(result.isSuccess)
        assertEquals(2, viewedDao.tracked.size)
        assertEquals(setOf(VACANCY_ID to SEEKER_ID), viewedDao.tracked.map { it.vacancyId to it.jobSeekerId }.toSet())
    }

    @Test
    fun `trackViewedVacancy returns VacancyNotFound when vacancy is missing`() = runBlocking {
        val repository = repository(vacancyDao = FakeVacancyDao(vacancy = null))

        val result = repository.trackViewedVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertIs<ViewedVacancyException.VacancyNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `trackViewedVacancy returns VacancyNotFound when vacancy is inactive`() = runBlocking {
        val repository = repository(vacancyDao = FakeVacancyDao(vacancy = vacancyEntity(status = VacancyStatusCode.PAUSED)))

        val result = repository.trackViewedVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertIs<ViewedVacancyException.VacancyNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `trackViewedVacancy returns SeekerProfileNotFound when seeker profile is missing`() = runBlocking {
        val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

        val result = repository.trackViewedVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

        assertIs<ViewedVacancyException.SeekerProfileNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `getViewedVacancies uses current seeker profile and pagination`() = runBlocking {
        val viewedDao = FakeViewedVacancyDao()
        val repository = repository(viewedDao = viewedDao)

        val result = repository.getViewedVacancies(userId = USER_ID, limit = 3, offset = 6)

        assertTrue(result.isSuccess)
        assertEquals(Triple(SEEKER_ID, 3, 6), viewedDao.lastListRequest)
    }

    @Test
    fun `getViewedVacancies maps viewedAt viewsCount and vacancy details`() = runBlocking {
        val repository = repository()

        val result = repository.getViewedVacancies(userId = USER_ID, limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        val viewed = result.getOrThrow().single()
        assertEquals(VIEWED_AT, viewed.viewedAt)
        assertEquals(2, viewed.viewsCount)
        assertEquals(VACANCY_ID, viewed.vacancy.id)
        assertEquals("Kotlin Backend Developer", viewed.vacancy.title)
    }

    @Test
    fun `getViewedVacancies marks favorites in bulk`() = runBlocking {
        val favoriteDao = FakeFavoriteVacancyDao(favoriteIds = setOf(VACANCY_ID))
        val repository = repository(favoriteVacancyDao = favoriteDao)

        val result = repository.getViewedVacancies(userId = USER_ID, limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrThrow().single().vacancy.isFavorite)
        assertEquals(SEEKER_ID to setOf(VACANCY_ID), favoriteDao.lastFavoriteIdsRequest)
    }

    @Test
    fun `getViewedVacancies returns SeekerProfileNotFound when seeker profile is missing`() = runBlocking {
        val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

        val result = repository.getViewedVacancies(userId = USER_ID, limit = 20, offset = 0)

        assertIs<ViewedVacancyException.SeekerProfileNotFound>(result.exceptionOrNull())
    }

    private fun repository(
        viewedDao: ViewedVacancyDao = FakeViewedVacancyDao(),
        seekerProfileDao: SeekerProfileDao = FakeSeekerProfileDao(),
        vacancyDao: VacancyDao = FakeVacancyDao(),
        favoriteVacancyDao: FavoriteVacancyDao = FakeFavoriteVacancyDao(),
    ) = DefaultViewedVacancyRepository(
        viewedVacancyDao = viewedDao,
        seekerProfileDao = seekerProfileDao,
        vacancyDao = vacancyDao,
        favoriteVacancyDao = favoriteVacancyDao,
    )

    private class FakeViewedVacancyDao : ViewedVacancyDao {
        val tracked = mutableListOf<ViewedVacancyEntity>()
        var lastListRequest: Triple<Long, Int, Int>? = null

        override suspend fun trackViewedVacancy(viewedVacancy: ViewedVacancyEntity): Boolean {
            tracked += viewedVacancy
            return true
        }

        override suspend fun getViewedVacancies(
            jobSeekerId: Long,
            limit: Int,
            offset: Int,
        ): List<ViewedVacancyWithDetailsEntity> {
            lastListRequest = Triple(jobSeekerId, limit, offset)
            return listOf(
                ViewedVacancyWithDetailsEntity(
                    viewedAt = VIEWED_AT,
                    viewsCount = 2,
                    vacancy = vacancyWithDetails(),
                )
            )
        }

        override suspend fun countViewsByVacancyIds(vacancyIds: Set<Long>): Map<Long, Int> {
            return vacancyIds.associateWith { 2 }
        }
    }

    private class FakeFavoriteVacancyDao(
        private val favoriteIds: Set<Long> = emptySet(),
    ) : FavoriteVacancyDao {
        var lastFavoriteIdsRequest: Pair<Long, Set<Long>>? = null

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

    private class FakeVacancyDao(
        private val vacancy: VacancyEntity? = vacancyEntity(),
    ) : VacancyDao {
        override suspend fun createVacancy(vacancy: VacancyEntity): Long = vacancy.id
        override suspend fun getVacancy(id: Long): VacancyEntity? = vacancy
        override suspend fun getVacancyWithDetails(id: Long): VacancyWithDetailsEntity? = vacancyWithDetails()
        override suspend fun updateVacancy(vacancy: VacancyEntity): Boolean = true
        override suspend fun deleteVacancy(id: Long): Boolean = true
        override suspend fun getVacancies(filter: VacancyFilter, limit: Int, offset: Int): List<VacancyEntity> = listOf(vacancyEntity())
        override suspend fun getVacanciesWithDetails(filter: VacancyFilter, limit: Int, offset: Int): List<VacancyWithDetailsEntity> = listOf(vacancyWithDetails())
        override suspend fun getAppliedVacancies(
            jobSeekerId: Long,
            limit: Int,
            offset: Int,
            applicationStatuses: List<ApplicationStatusCode>,
        ): List<VacancyWithDetailsEntity> = listOf(vacancyWithDetails())
        override suspend fun countVacancies(filter: VacancyFilter): Int = 1
        override suspend fun getVacanciesByCompany(companyId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> = listOf(vacancyWithDetails())
        override suspend fun getRecentVacancies(limit: Int): List<VacancyEntity> = listOf(vacancyEntity())
        override suspend fun updateVacancyStatus(id: Long, status: VacancyStatusCode): Boolean = true
        override suspend fun vacancyExists(id: Long): Boolean = vacancy != null
    }

    private companion object {
        const val USER_ID = 7L
        const val SEEKER_ID = 11L
        const val VACANCY_ID = 13L
        val NOW: Instant = Instant.parse("2026-05-04T00:00:00Z")
        val VIEWED_AT: Instant = Instant.parse("2026-05-13T12:00:00Z")

        fun seekerProfile() = SeekerProfileEntity(
            id = SEEKER_ID,
            userId = USER_ID,
            specialty = "Backend Developer",
            experienceYears = 5,
            desiredSalary = 5000,
            aboutMe = null,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
        )

        fun vacancyEntity(status: VacancyStatusCode = VacancyStatusCode.ACTIVE) = VacancyEntity(
            id = VACANCY_ID,
            companyId = 17L,
            title = "Kotlin Backend Developer",
            description = "Build Ktor services",
            salaryMin = 4000,
            salaryMax = 6000,
            minExperienceYears = 3,
            employmentType = null,
            category = JobCategoryCode.SOFTWARE_DEV,
            status = status,
            createdAt = NOW,
            updatedAt = NOW,
        )

        fun vacancyWithDetails() = VacancyWithDetailsEntity(
            vacancy = vacancyEntity(),
            company = CompanyEntity(
                id = 17L,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            )
        )
    }
}
