package not.djinni.domain.usecase.vacancy

import not.djinni.database.api.vacancy.VacancyFilter
import not.djinni.database.api.vacancy.VacancyTitleRelevance
import not.djinni.domain.repository.SeekerProfileRepository
import not.djinni.domain.repository.VacancyRepository
import not.djinni.domain.usecase.core.UseCaseWithParams
import not.djinni.domain.usecase.vacancy.GetRecommendedVacanciesForSeekerUseCase.Params
import not.djinni.model.role.WorkExperience
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
            categories = listOf(seekerProfile.jobCategory),
            titleRelevance = buildTitleRelevance(
                speciality = seekerProfile.speciality,
                workExperience = seekerProfile.workExperience,
            ),
        )
        return vacancyRepository.getPublicVacanciesForSeeker(
            userId = params.userId,
            filter = filter,
            limit = params.limit,
            offset = params.offset,
        )
    }

    private fun buildTitleRelevance(
        speciality: String,
        workExperience: List<WorkExperience>,
    ): VacancyTitleRelevance? {
        val primaryPhrase = speciality.trim()
        if (primaryPhrase.isEmpty()) return null

        val secondaryPhrases = workExperience
            .sortedWith(compareBy<WorkExperience> { !it.isCurrent }.thenByDescending { it.startDate })
            .map { it.position.trim() }
            .filter { it.isNotEmpty() && !it.equals(primaryPhrase, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .take(3)

        val tokens = buildList {
            addAll(tokenize(primaryPhrase))
            secondaryPhrases.forEach { addAll(tokenize(it)) }
        }.distinct().take(8)

        return VacancyTitleRelevance(
            primaryPhrase = primaryPhrase,
            secondaryPhrases = secondaryPhrases,
            tokens = tokens,
        )
    }

    private fun tokenize(input: String): List<String> {
        return input
            .lowercase()
            .split(Regex("[^\\p{L}\\p{Nd}]+"))
            .filter { it.length >= 2 }
    }

    data class Params(
        val userId: Long,
        val query: String,
        val limit: Int,
        val offset: Int,
    )
}
