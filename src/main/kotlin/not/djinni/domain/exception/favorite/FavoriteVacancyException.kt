package not.djinni.domain.exception.favorite

sealed class FavoriteVacancyException(override val message: String) : Throwable() {
    class Unauthorized : FavoriteVacancyException("Unauthorized to access favorite vacancies")
    class VacancyNotFound : FavoriteVacancyException("Vacancy not found")
    class SeekerProfileNotFound : FavoriteVacancyException("Seeker profile does not exist")
    class FavoriteAlreadyExists : FavoriteVacancyException("Vacancy is already added to favorites")
    class FavoriteNotFound : FavoriteVacancyException("Vacancy is not added to favorites")
}
