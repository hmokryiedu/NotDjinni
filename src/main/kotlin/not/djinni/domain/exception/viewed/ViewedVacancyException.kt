package not.djinni.domain.exception.viewed

sealed class ViewedVacancyException(override val message: String) : Throwable() {
    class Unauthorized : ViewedVacancyException("Unauthorized to access viewed vacancies")
    class VacancyNotFound : ViewedVacancyException("Vacancy not found")
    class SeekerProfileNotFound : ViewedVacancyException("Seeker profile does not exist")
}
