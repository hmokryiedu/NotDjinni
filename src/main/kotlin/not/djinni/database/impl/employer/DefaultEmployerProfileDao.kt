package not.djinni.database.impl.employer

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Single

@Single([EmployerProfileDao::class])
class DefaultEmployerProfileDao : EmployerProfileDao {

    override suspend fun createProfile(profile: EmployerProfileEntity): Long = runQuery {
        EmployerProfileTableEntity.new {
            userId = EntityID(profile.userId, UserTable)
            company = CompanyTableEntity.findById(profile.companyId)
                ?: throw IllegalArgumentException("Company not found: ${profile.companyId}")
            role = profile.role
        }.id.value
    }

    override suspend fun getProfile(id: Long): EmployerProfileWithCompany? = runQuery {
        EmployerProfileTableEntity.findById(id)?.let {
            EmployerProfileWithCompany(
                profile = it.toEntity(),
                company = it.company.toEntity()
            )
        }
    }

    override suspend fun getProfileByUserId(userId: Long): EmployerProfileWithCompany? = runQuery {
        EmployerProfileTableEntity.find { EmployerProfileTable.userId eq userId }
            .firstOrNull()
            ?.let {
                EmployerProfileWithCompany(
                    profile = it.toEntity(),
                    company = it.company.toEntity()
                )
            }
    }

    override suspend fun updateProfile(profile: EmployerProfileEntity): Boolean = runQuery {
        EmployerProfileTableEntity.findById(profile.id)?.apply {
            role = profile.role
        } != null
    }

    override suspend fun deleteProfile(id: Long): Boolean = runQuery {
        EmployerProfileTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun profileExists(userId: Long): Boolean = runQuery {
        !EmployerProfileTableEntity.find { EmployerProfileTable.userId eq userId }.empty()
    }
}
