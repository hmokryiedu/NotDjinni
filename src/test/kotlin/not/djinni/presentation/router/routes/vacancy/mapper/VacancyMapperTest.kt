package not.djinni.presentation.router.routes.vacancy.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.Instant
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.Salary
import not.djinni.model.vacancy.VacancyStatusCode
import not.djinni.model.vacancy.VacancyWithDetails
import not.djinni.model.role.Company

class VacancyMapperTest {

    @Test
    fun `maps applications count to vacancy details response`() {
        val vacancy = VacancyWithDetails(
            id = 1,
            company = Company(
                id = 2,
                companyName = "Acme",
                website = null,
                description = "Product company"
            ),
            title = "Backend Engineer",
            description = "Kotlin backend",
            salary = Salary(min = 1000, max = 2000),
            minExperienceYears = 3,
            employmentType = EmploymentTypeCode.FULL_TIME,
            category = JobCategoryCode.SOFTWARE_DEV,
            status = VacancyStatusCode.ACTIVE,
            applicationsCount = 7,
            createdAt = Instant.parse("2026-05-04T00:00:00Z"),
            updatedAt = Instant.parse("2026-05-04T00:00:00Z")
        )

        val response = vacancy.toResponse()

        assertEquals(7, response.applicationsCount)
    }
}
