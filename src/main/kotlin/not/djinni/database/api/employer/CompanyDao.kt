package not.djinni.database.api.employer

interface CompanyDao {
    suspend fun createCompany(company: CompanyEntity): Long
    suspend fun getCompany(id: Long): CompanyEntity?
    suspend fun getCompanyByName(name: String): CompanyEntity?
    suspend fun getAllCompanies(): List<CompanyEntity>
    suspend fun searchCompaniesByName(name: String): List<CompanyEntity>
    suspend fun updateCompany(company: CompanyEntity): Boolean
    suspend fun deleteCompany(id: Long): Boolean
    suspend fun companyExists(name: String): Boolean
}
