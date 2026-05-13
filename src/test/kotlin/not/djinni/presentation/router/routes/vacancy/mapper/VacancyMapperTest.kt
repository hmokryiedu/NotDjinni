package not.djinni.presentation.router.routes.vacancy.mapper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
        val vacancy = vacancy(applicationsCount = 7)

        val response = vacancy.toResponse()

        assertEquals(7, response.applicationsCount)
    }

    @Test
    fun `guest mapper omits is_favorite`() {
        val response = vacancy(isFavorite = false).toGuestResponse()

        val json = json.encodeToString(response)

        assertFalse(json.contains("is_favorite"))
    }

    @Test
    fun `emits is_favorite true and false when favorite state is present`() {
        val favoriteJson = json.encodeToString(vacancy(isFavorite = true).toResponse())
        val notFavoriteJson = json.encodeToString(vacancy(isFavorite = false).toResponse())

        assertTrue(json.parseToJsonElement(favoriteJson).jsonObject["is_favorite"]?.jsonPrimitive?.boolean == true)
        assertTrue(json.parseToJsonElement(notFavoriteJson).jsonObject["is_favorite"]?.jsonPrimitive?.boolean == false)
    }

    @Test
    fun `authenticated vacancy list response omits application fields`() {
        val json = json.encodeToString(listOf(vacancy()).toResponseList())

        assertFalse(json.contains("application_id"))
        assertFalse(json.contains("cover_letter"))
        assertFalse(json.contains("job_seeker"))
    }

    private fun vacancy(
        applicationsCount: Int = 0,
        isFavorite: Boolean = false,
    ) = VacancyWithDetails(
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
        applicationsCount = applicationsCount,
        isFavorite = isFavorite,
        createdAt = Instant.parse("2026-05-04T00:00:00Z"),
        updatedAt = Instant.parse("2026-05-04T00:00:00Z")
    )

    private companion object {
        val json = Json { explicitNulls = false }
    }
}
