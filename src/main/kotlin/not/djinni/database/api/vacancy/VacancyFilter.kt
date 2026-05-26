package not.djinni.database.api.vacancy

import not.djinni.database.api.common.SortDirection
import not.djinni.model.vacancy.EmploymentTypeCode
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.model.vacancy.VacancyStatusCode

enum class VacancySortField {
    CREATED_AT,
    UPDATED_AT,
    SALARY_MIN,
    SALARY_MAX,
    TITLE,
    MIN_EXPERIENCE
}

data class VacancyFilter(
    val companyId: Long? = null,
    val categories: List<JobCategoryCode> = emptyList(),
    val statuses: List<VacancyStatusCode> = emptyList(),
    val employmentTypes: List<EmploymentTypeCode> = emptyList(),
    val salaryMin: Int? = null,
    val salaryMax: Int? = null,
    val experienceYears: Int? = null,
    val searchQuery: String? = null,
    val titleRelevance: VacancyTitleRelevance? = null,
    val sortBy: VacancySortField = VacancySortField.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC
)

data class VacancyTitleRelevance(
    val primaryPhrase: String,
    val secondaryPhrases: List<String> = emptyList(),
    val tokens: List<String> = emptyList(),
)
