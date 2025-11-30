package not.djinni.data.repository

import not.djinni.data.mapper.toDomain
import not.djinni.data.mapper.toEntity
import not.djinni.database.api.employer.CompanyDao
import not.djinni.domain.exception.employer.CompanyException
import not.djinni.domain.repository.CompanyRepository
import not.djinni.model.role.Company
import org.koin.core.annotation.Single

@Single(binds = [CompanyRepository::class])
class DefaultCompanyRepository(
    private val companyDao: CompanyDao
) : CompanyRepository {

    override suspend fun getCompany(id: Long) = runCatching {
        companyDao.getCompany(id)?.toDomain()
            ?: throw CompanyException.CompanyNotFound()
    }

    override suspend fun getAllCompanies() = runCatching {
        companyDao.getAllCompanies().map { it.toDomain() }
    }

    override suspend fun searchCompanies(name: String) = runCatching {
        companyDao.searchCompaniesByName(name).map { it.toDomain() }
    }

    override suspend fun createCompany(company: Company) = runCatching {
        if (companyDao.companyExists(company.companyName)) {
            throw CompanyException.CompanyAlreadyExists()
        }
        val companyId = companyDao.createCompany(company.toEntity())
        companyDao.getCompany(companyId)?.toDomain()
            ?: throw CompanyException.CompanyNotFound()
    }

    override suspend fun updateCompany(company: Company) = runCatching<Unit> {
        val updated = companyDao.updateCompany(company.toEntity())
        if (!updated) throw CompanyException.CompanyNotFound()
    }

    override suspend fun deleteCompany(id: Long) = runCatching<Unit> {
        val deleted = companyDao.deleteCompany(id)
        if (!deleted) throw CompanyException.CompanyNotFound()
    }
}
