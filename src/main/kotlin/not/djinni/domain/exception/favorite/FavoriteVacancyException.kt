package not.djinni.domain.exception.favorite

sealed class FavoriteVacancyException(override val message: String) : Throwable() {
    class Unauthorized : FavoriteVacancyException("Unauthorized to access favorite vacancies")
    class VacancyNotFound : FavoriteVacancyException("Vacancy not found")
}
