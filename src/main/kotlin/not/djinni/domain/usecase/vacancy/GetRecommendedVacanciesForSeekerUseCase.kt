package not.djinni.domain.usecase.vacancy

import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.domain.repository.SeekerProfileRepository
import not.djinni.domain.repository.VacancyRepository
import not.djinni.domain.usecase.core.UseCaseWithParams
import not.djinni.domain.usecase.vacancy.GetRecommendedVacanciesForSeekerUseCase.Params
import not.djinni.model.vacancy.VacancyWithDetails
import org.koin.core.annotation.Factory

@Factory
class GetRecommendedVacanciesForSeekerUseCase(
    private val vacancyRepository: VacancyRepository,
    private val seekerRepository: SeekerProfileRepository,
) : UseCaseWithParams<Result<List<VacancyWithDetails>>, Params> {

    override suspend fun invoke(params: Params): Result<List<VacancyWithDetails>> {
        val seekerProfile = seekerRepository.getProfile(params.userId).getOrThrow()
        val filter = VacancyFilter(
            searchQuery = params.query,
            salaryMin = seekerProfile.desiredSalary,
            experienceYears = seekerProfile.experienceYears,
            categories = listOf(seekerProfile.jobCategory)
        )
        return vacancyRepository.getVacancies(filter = filter, limit = params.limit, offset = params.offset)
    }

    data class Params(
        val userId: Long,
        val query: String,
        val limit: Int,
        val offset: Int,
    )
}