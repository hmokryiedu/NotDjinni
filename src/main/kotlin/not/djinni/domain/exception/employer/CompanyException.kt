package not.djinni.domain.exception.employer

sealed class CompanyException(override val message: String) : Throwable() {
    class CompanyNotFound : CompanyException("Company not found")
    class CompanyAlreadyExists : CompanyException("Company with this name already exists")
    data class InvalidCompanyData(override val message: String) : CompanyException(message)
}
