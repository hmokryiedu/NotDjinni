package not.djinni.domain.exception.vacancy

sealed class VacancyException(override val message: String) : Throwable() {
    class VacancyNotFound : VacancyException("Vacancy not found")
    class Unauthorized(message: String = "Unauthorized to access this vacancy") : VacancyException(message)
    data class InvalidVacancyData(override val message: String) : VacancyException(message)
}
