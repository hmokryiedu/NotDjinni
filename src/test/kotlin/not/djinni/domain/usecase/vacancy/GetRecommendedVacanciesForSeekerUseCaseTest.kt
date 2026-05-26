package not.djinni.domain.usecase.vacancy

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancyTitleRelevance
import not.djinni.domain.repository.SeekerProfileRepository
import not.djinni.domain.repository.VacancyRepository
import not.djinni.model.application.ApplicationStatusCode
import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.Salary
import not.djinni.model.vacancy.Vacancy
import not.djinni.model.vacancy.VacancyStatusCode
import not.djinni.model.vacancy.VacancyWithDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRecommendedVacanciesForSeekerUseCaseTest {

    @Test
    fun `recommended vacancies keep views count from repository`() = runBlocking {
        val vacancyRepository = FakeVacancyRepository(
            vacancies = listOf(vacancyWithDetails(viewsCount = 25))
        )
        val useCase = GetRecommendedVacanciesForSeekerUseCase(
            vacancyRepository = vacancyRepository,
            seekerRepository = FakeSeekerProfileRepository(),
        )

        val result = useCase(
            GetRecommendedVacanciesForSeekerUseCase.Params(
                userId = USER_ID,
                query = "Kotlin",
                limit = 20,
                offset = 0,
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(25, result.getOrThrow().single().viewsCount)
        assertEquals(USER_ID, vacancyRepository.lastPublicForSeekerUserId)
        val passedFilter = vacancyRepository.lastPublicForSeekerFilter
        assertEquals("Kotlin", passedFilter?.searchQuery)
        assertEquals(5000, passedFilter?.salaryMin)
        assertEquals(5, passedFilter?.experienceYears)
        assertEquals(listOf(JobCategoryCode.SOFTWARE_DEV), passedFilter?.categories)
        assertEquals(
            VacancyTitleRelevance(
                primaryPhrase = "Backend Developer",
                secondaryPhrases = listOf("Senior Kotlin Engineer"),
                tokens = listOf("backend", "developer", "senior", "kotlin", "engineer"),
            ),
            passedFilter?.titleRelevance
        )
    }

    private class FakeVacancyRepository(
        private val vacancies: List<VacancyWithDetails>,
    ) : VacancyRepository {
        var lastPublicForSeekerUserId: Long? = null
        var lastPublicForSeekerFilter: VacancyFilter? = null

        override suspend fun createVacancy(userId: Long, vacancy: Vacancy): Result<VacancyWithDetails> = error("Not needed")
        override suspend fun getVacancy(id: Long): Result<Vacancy> = error("Not needed")
        override suspend fun getVacancyWithDetails(id: Long): Result<VacancyWithDetails> = error("Not needed")
        override suspend fun updateVacancy(userId: Long, vacancy: Vacancy): Result<Unit> = error("Not needed")
        override suspend fun deleteVacancy(userId: Long, id: Long): Result<Unit> = error("Not needed")
        override suspend fun getVacancies(filter: VacancyFilter, limit: Int, offset: Int): Result<List<VacancyWithDetails>> {
            throw AssertionError("Generic getVacancies must not be used for recommendations")
        }
        override suspend fun getPublicVacancies(filter: VacancyFilter, limit: Int, offset: Int): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun getPublicVacanciesForSeeker(userId: Long, filter: VacancyFilter, limit: Int, offset: Int): Result<List<VacancyWithDetails>> {
            lastPublicForSeekerUserId = userId
            lastPublicForSeekerFilter = filter
            return Result.success(vacancies)
        }
        override suspend fun getAppliedVacancies(
            userId: Long,
            limit: Int,
            offset: Int,
            applicationStatuses: List<ApplicationStatusCode>,
        ): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun getPublicVacancyWithDetails(id: Long): Result<VacancyWithDetails> = error("Not needed")
        override suspend fun getPublicVacancyWithDetailsForSeeker(userId: Long, id: Long): Result<VacancyWithDetails> = error("Not needed")
        override suspend fun getPublicRecentVacancies(limit: Int): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun getPublicCompanyVacancies(companyId: Long, limit: Int, offset: Int): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun countVacancies(filter: VacancyFilter): Result<Int> = error("Not needed")
        override suspend fun getEmployerVacancies(userId: Long, limit: Int, offset: Int): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun getCompanyVacancies(companyId: Long, limit: Int, offset: Int): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun getRecentVacancies(limit: Int): Result<List<VacancyWithDetails>> = error("Not needed")
        override suspend fun updateVacancyStatus(userId: Long, id: Long, status: VacancyStatusCode): Result<Unit> = error("Not needed")
    }

    private class FakeSeekerProfileRepository : SeekerProfileRepository {
        override suspend fun getProfile(userId: Long): Result<SeekerProfile> = Result.success(seekerProfile())
        override suspend fun createProfile(userId: Long, profile: SeekerProfile): Result<SeekerProfile> = error("Not needed")
        override suspend fun updateProfile(userId: Long, profile: SeekerProfile): Result<SeekerProfile> = error("Not needed")
        override suspend fun deleteProfile(userId: Long): Result<Unit> = error("Not needed")
        override suspend fun addWorkExperience(userId: Long, experience: WorkExperience): Result<SeekerProfile> = error("Not needed")
        override suspend fun updateWorkExperience(userId: Long, experienceId: Long, experience: WorkExperience): Result<SeekerProfile> = error("Not needed")
        override suspend fun deleteWorkExperience(userId: Long, experienceId: Long): Result<SeekerProfile> = error("Not needed")
    }

    private companion object {
        const val USER_ID = 7L
        val NOW: Instant = Instant.parse("2026-05-04T00:00:00Z")

        fun seekerProfile() = SeekerProfile(
            id = 11L,
            aboutMe = null,
            speciality = "Backend Developer",
            desiredSalary = 5000,
            experienceYears = 5,
            jobCategory = JobCategoryCode.SOFTWARE_DEV,
            workExperience = listOf(
                WorkExperience(
                    id = 1L,
                    companyName = "Now",
                    position = "Senior Kotlin Engineer",
                    description = null,
                    startDate = Instant.parse("2025-01-01T00:00:00Z"),
                    endDate = null,
                ),
                WorkExperience(
                    id = 2L,
                    companyName = "Before",
                    position = "Backend Developer",
                    description = null,
                    startDate = Instant.parse("2023-01-01T00:00:00Z"),
                    endDate = Instant.parse("2024-12-31T00:00:00Z"),
                ),
            )
        )

        fun vacancyWithDetails(viewsCount: Int) = VacancyWithDetails(
            id = 13L,
            company = not.djinni.model.role.Company(
                id = 17L,
                companyName = "Not Djinni",
                website = null,
                description = "Hiring platform",
            ),
            title = "Kotlin Backend Developer",
            description = "Build Ktor services",
            salary = Salary(min = 4000, max = 6000),
            minExperienceYears = 3,
            employmentType = EmploymentTypeCode.FULL_TIME,
            category = JobCategoryCode.SOFTWARE_DEV,
            status = VacancyStatusCode.ACTIVE,
            createdAt = NOW,
            updatedAt = NOW,
            viewsCount = viewsCount,
        )
    }
}
