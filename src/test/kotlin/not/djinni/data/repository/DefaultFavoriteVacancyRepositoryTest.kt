package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.favorite.FavoriteVacancyDao
import not.djinni.database.api.favorite.FavoriteVacancyEntity
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.database.api.vacancy.VacancyDao
import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.domain.exception.favorite.FavoriteVacancyException
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultFavoriteVacancyRepositoryTest {

    @Test
    fun `addFavoriteVacancy stores seeker favorite when vacancy exists`() {
        runBlocking {
            val favoriteDao = FakeFavoriteVacancyDao()
            val repository = repository(favoriteDao = favoriteDao)

            val result = repository.addFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertTrue(result.isSuccess)
            assertEquals(VACANCY_ID, favoriteDao.created.single().vacancyId)
            assertEquals(SEEKER_ID, favoriteDao.created.single().jobSeekerId)
        }
    }

    @Test
    fun `addFavoriteVacancy returns VacancyNotFound when vacancy is missing`() {
        runBlocking {
            val repository = repository(vacancyDao = FakeVacancyDao(vacancy = null))

            val result = repository.addFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertIs<FavoriteVacancyException.VacancyNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `addFavoriteVacancy returns VacancyNotFound when vacancy is inactive`() {
        runBlocking {
            val repository = repository(vacancyDao = FakeVacancyDao(vacancy = vacancyEntity(status = VacancyStatusCode.PAUSED)))

            val result = repository.addFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertIs<FavoriteVacancyException.VacancyNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `addFavoriteVacancy returns SeekerProfileNotFound when seeker profile is missing`() {
        runBlocking {
            val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

            val result = repository.addFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertIs<FavoriteVacancyException.SeekerProfileNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `addFavoriteVacancy returns FavoriteAlreadyExists for duplicate favorite`() {
        runBlocking {
            val favoriteDao = FakeFavoriteVacancyDao(favoriteExists = true)
            val repository = repository(favoriteDao = favoriteDao)

            val result = repository.addFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertIs<FavoriteVacancyException.FavoriteAlreadyExists>(result.exceptionOrNull())
            assertEquals(emptyList(), favoriteDao.created)
        }
    }

    @Test
    fun `removeFavoriteVacancy returns SeekerProfileNotFound when seeker profile is missing`() {
        runBlocking {
            val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

            val result = repository.removeFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertIs<FavoriteVacancyException.SeekerProfileNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `removeFavoriteVacancy returns FavoriteNotFound for missing favorite`() {
        runBlocking {
            val favoriteDao = FakeFavoriteVacancyDao(favoriteExists = false)
            val repository = repository(favoriteDao = favoriteDao)

            val result = repository.removeFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertIs<FavoriteVacancyException.FavoriteNotFound>(result.exceptionOrNull())
            assertEquals(emptyList(), favoriteDao.removed)
        }
    }

    @Test
    fun `removeFavoriteVacancy removes existing favorite`() {
        runBlocking {
            val favoriteDao = FakeFavoriteVacancyDao(favoriteExists = true)
            val repository = repository(favoriteDao = favoriteDao)

            val result = repository.removeFavoriteVacancy(userId = USER_ID, vacancyId = VACANCY_ID)

            assertTrue(result.isSuccess)
            assertEquals(VACANCY_ID to SEEKER_ID, favoriteDao.removed.single())
        }
    }

    @Test
    fun `getFavoriteVacancies returns vacancies with details for current seeker`() {
        runBlocking {
            val repository = repository()

            val result = repository.getFavoriteVacancies(userId = USER_ID, limit = 20, offset = 0)

            assertTrue(result.isSuccess)
            assertEquals(listOf(VACANCY_ID), result.getOrThrow().map { it.id })
        }
    }

    @Test
    fun `getFavoriteVacancies keeps views count`() {
        runBlocking {
            val favoriteDao = FakeFavoriteVacancyDao(favoriteVacancies = listOf(vacancyWithDetails(viewsCount = 21)))
            val repository = repository(favoriteDao = favoriteDao)

            val result = repository.getFavoriteVacancies(userId = USER_ID, limit = 20, offset = 0)

            assertTrue(result.isSuccess)
            assertEquals(21, result.getOrThrow().single().viewsCount)
        }
    }

    @Test
    fun `getFavoriteVacancies returns SeekerProfileNotFound when seeker profile is missing`() {
        runBlocking {
            val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

            val result = repository.getFavoriteVacancies(userId = USER_ID, limit = 20, offset = 0)

            assertIs<FavoriteVacancyException.SeekerProfileNotFound>(result.exceptionOrNull())
        }
    }

    @Test
    fun `getFavoriteVacancyIds returns ids for current seeker and requested vacancy ids`() {
        runBlocking {
            val favoriteDao = FakeFavoriteVacancyDao(favoriteIds = setOf(VACANCY_ID))
            val repository = repository(favoriteDao = favoriteDao)

            val result = repository.getFavoriteVacancyIds(userId = USER_ID, vacancyIds = setOf(VACANCY_ID, 99L))

            assertTrue(result.isSuccess)
            assertEquals(setOf(VACANCY_ID), result.getOrThrow())
            assertEquals(SEEKER_ID to setOf(VACANCY_ID, 99L), favoriteDao.lastFavoriteIdsRequest)
        }
    }

    @Test
    fun `getFavoriteVacancyIds returns SeekerProfileNotFound when seeker profile is missing`() {
        runBlocking {
            val repository = repository(seekerProfileDao = FakeSeekerProfileDao(profile = null))

            val result = repository.getFavoriteVacancyIds(userId = USER_ID, vacancyIds = setOf(VACANCY_ID))

            assertIs<FavoriteVacancyException.SeekerProfileNotFound>(result.exceptionOrNull())
        }
    }

    private fun repository(
        favoriteDao: FavoriteVacancyDao = FakeFavoriteVacancyDao(),
        seekerProfileDao: SeekerProfileDao = FakeSeekerProfileDao(),
        vacancyDao: VacancyDao = FakeVacancyDao(),
    ) = DefaultFavoriteVacancyRepository(
        favoriteVacancyDao = favoriteDao,
        seekerProfileDao = seekerProfileDao,
        vacancyDao = vacancyDao,
    )

    private class FakeFavoriteVacancyDao(
        private val favoriteExists: Boolean = false,
        private val favoriteIds: Set<Long> = emptySet(),
        private val favoriteVacancies: List<VacancyWithDetailsEntity> = listOf(vacancyWithDetails()),
    ) : FavoriteVacancyDao {
        val created = mutableListOf<FavoriteVacancyEntity>()
        val removed = mutableListOf<Pair<Long, Long>>()
        var lastFavoriteIdsRequest: Pair<Long, Set<Long>>? = null

        override suspend fun addFavoriteVacancy(favorite: FavoriteVacancyEntity): Boolean {
            created += favorite
            return true
        }

        override suspend fun removeFavoriteVacancy(vacancyId: Long, jobSeekerId: Long): Boolean {
            removed += vacancyId to jobSeekerId
            return true
        }

        override suspend fun getFavoriteVacancies(jobSeekerId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> {
            return favoriteVacancies
        }

        override suspend fun favoriteExists(vacancyId: Long, jobSeekerId: Long): Boolean = favoriteExists || created.any {
            it.vacancyId == vacancyId && it.jobSeekerId == jobSeekerId
        }

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
        override suspend fun getAppliedVacancies(jobSeekerId: Long, limit: Int, offset: Int): List<VacancyWithDetailsEntity> = listOf(vacancyWithDetails())
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

        fun seekerProfile() = SeekerProfileEntity(
            id = SEEKER_ID,
            userId = USER_ID,
            specialty = "Backend Developer",
            experienceYears = 5,
            desiredSalary = 5000,
            aboutMe = null,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
        )

        fun vacancyEntity(
            status: VacancyStatusCode = VacancyStatusCode.ACTIVE,
            viewsCount: Int = 0,
        ) = VacancyEntity(
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
            viewsCount = viewsCount,
        )

        fun vacancyWithDetails(viewsCount: Int = 0) = VacancyWithDetailsEntity(
            vacancy = vacancyEntity(viewsCount = viewsCount),
            company = CompanyEntity(
                id = 17L,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
            viewsCount = viewsCount,
        )
    }
}
