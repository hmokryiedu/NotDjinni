package not.djinni.domain.repository

import not.djinni.model.role.Company

interface CompanyRepository {
    suspend fun getCompany(id: Long): Result<Company>
    suspend fun getAllCompanies(): Result<List<Company>>
    suspend fun searchCompanies(name: String): Result<List<Company>>
    suspend fun createCompany(company: Company): Result<Company>
    suspend fun updateCompany(company: Company): Result<Unit>
    suspend fun deleteCompany(id: Long): Result<Unit>
}
