package not.djinni.domain.exception.employer

sealed class EmployerProfileException(override val message: String) : Throwable() {
    class ProfileNotFound : EmployerProfileException("Employer profile not found")
    class ProfileAlreadyExists : EmployerProfileException("Employer profile already exists for this user")
    class CompanyNotFound : EmployerProfileException("Company not found")
    class Unauthorized : EmployerProfileException("Unauthorized to access this resource")
    data class InvalidProfileData(override val message: String) : EmployerProfileException(message)
}
