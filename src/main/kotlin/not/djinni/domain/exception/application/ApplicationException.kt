package not.djinni.domain.exception.application

sealed class ApplicationException(override val message: String) : Throwable() {
    class ApplicationNotFound : ApplicationException("Application not found")
    class AlreadyApplied : ApplicationException("You have already applied to this vacancy")
    class Unauthorized : ApplicationException("Unauthorized to access this application")
    class VacancyNotFound : ApplicationException("Vacancy not found")
    class SeekerProfileNotFound : ApplicationException("Job seeker profile not found")
    data class InvalidApplicationData(override val message: String) : ApplicationException(message)
}
