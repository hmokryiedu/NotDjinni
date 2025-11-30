package not.djinni.database.impl.employer

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.CompanyDao
import not.djinni.database.api.employer.CompanyEntity
import org.koin.core.annotation.Single

@Single([CompanyDao::class])
class DefaultCompanyDao : CompanyDao {

    override suspend fun createCompany(company: CompanyEntity): Long = runQuery {
        CompanyTableEntity.new {
            companyName = company.companyName
            website = company.website
            description = company.description
        }.id.value
    }

    override suspend fun getCompany(id: Long): CompanyEntity? = runQuery {
        CompanyTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getCompanyByName(name: String): CompanyEntity? = runQuery {
        CompanyTableEntity.find { CompanyTable.companyName eq name }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun getAllCompanies(): List<CompanyEntity> = runQuery {
        CompanyTableEntity.all().map { it.toEntity() }
    }

    override suspend fun searchCompaniesByName(name: String): List<CompanyEntity> = runQuery {
        CompanyTableEntity.find { CompanyTable.companyName like "%$name%" }
            .map { it.toEntity() }
    }

    override suspend fun updateCompany(company: CompanyEntity): Boolean = runQuery {
        CompanyTableEntity.findById(company.id)?.apply {
            companyName = company.companyName
            website = company.website
            description = company.description
        } != null
    }

    override suspend fun deleteCompany(id: Long): Boolean = runQuery {
        CompanyTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun companyExists(name: String): Boolean = runQuery {
        !CompanyTableEntity.find { CompanyTable.companyName eq name }.empty()
    }
}
