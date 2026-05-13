package not.djinni.data.repository

import kotlinx.coroutines.runBlocking
import not.djinni.database.api.employer.CompanyDao
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany
import not.djinni.domain.exception.employer.EmployerProfileException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultEmployerProfileRepositoryTest {

    @Test
    fun `updateProfile updates role returns updated profile and keeps company`() = runBlocking {
        val employerProfileDao = FakeEmployerProfileDao()
        val repository = repository(employerProfileDao = employerProfileDao)

        val result = repository.updateProfile(USER_ID, "Recruiter")

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals("Recruiter", updated.role)
        assertEquals(COMPANY_ID, updated.company.id)
        assertEquals("NotDjinni", updated.company.companyName)
        assertEquals(COMPANY_ID, employerProfileDao.updated.single().companyId)
    }

    @Test
    fun `updateProfile returns ProfileNotFound when employer profile is missing`() = runBlocking {
        val repository = repository(employerProfileDao = FakeEmployerProfileDao(profile = null))

        val result = repository.updateProfile(USER_ID, "Recruiter")

        assertIs<EmployerProfileException.ProfileNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `updateProfile returns InvalidProfileData for blank role`() = runBlocking {
        val repository = repository()

        val result = repository.updateProfile(USER_ID, " ")

        assertIs<EmployerProfileException.InvalidProfileData>(result.exceptionOrNull())
    }

    private fun repository(
        employerProfileDao: EmployerProfileDao = FakeEmployerProfileDao(),
        companyDao: CompanyDao = FakeCompanyDao(),
    ) = DefaultEmployerProfileRepository(employerProfileDao, companyDao)

    private class FakeEmployerProfileDao(
        profile: EmployerProfileEntity? = employerProfileEntity(),
        private val company: CompanyEntity = companyEntity(),
    ) : EmployerProfileDao {
        private var profile = profile
        val updated = mutableListOf<EmployerProfileEntity>()

        override suspend fun createProfile(profile: EmployerProfileEntity): Long = PROFILE_ID
        override suspend fun getProfile(id: Long): EmployerProfileWithCompany? = withCompany(profile?.takeIf { it.id == id })
        override suspend fun getProfileByUserId(userId: Long): EmployerProfileWithCompany? = withCompany(profile?.takeIf { it.userId == userId })

        override suspend fun updateProfile(profile: EmployerProfileEntity): Boolean {
            this.profile = profile
            updated += profile
            return true
        }

        override suspend fun deleteProfile(id: Long): Boolean = true
        override suspend fun profileExists(userId: Long): Boolean = profile != null

        private fun withCompany(profile: EmployerProfileEntity?): EmployerProfileWithCompany? {
            return profile?.let { EmployerProfileWithCompany(it, company) }
        }
    }

    private class FakeCompanyDao : CompanyDao {
        override suspend fun createCompany(company: CompanyEntity): Long = COMPANY_ID
        override suspend fun getCompany(id: Long): CompanyEntity? = companyEntity().takeIf { it.id == id }
        override suspend fun getCompanyByName(name: String): CompanyEntity? = companyEntity().takeIf { it.companyName == name }
        override suspend fun getAllCompanies(): List<CompanyEntity> = listOf(companyEntity())
        override suspend fun searchCompaniesByName(name: String): List<CompanyEntity> = listOf(companyEntity()).filter {
            it.companyName.contains(name, ignoreCase = true)
        }
        override suspend fun updateCompany(company: CompanyEntity): Boolean = true
        override suspend fun deleteCompany(id: Long): Boolean = true
        override suspend fun companyExists(name: String): Boolean = companyEntity().companyName == name
    }

    private companion object {
        const val USER_ID = 10L
        const val PROFILE_ID = 20L
        const val COMPANY_ID = 30L

        fun employerProfileEntity(
            id: Long = PROFILE_ID,
            userId: Long = USER_ID,
            companyId: Long = COMPANY_ID,
            role: String = "Hiring Manager",
        ) = EmployerProfileEntity(
            id = id,
            userId = userId,
            companyId = companyId,
            role = role,
        )

        fun companyEntity(
            id: Long = COMPANY_ID,
            companyName: String = "NotDjinni",
            website: String? = "https://example.com",
            description: String = "Hiring",
        ) = CompanyEntity(
            id = id,
            companyName = companyName,
            website = website,
            description = description,
        )
    }
}
