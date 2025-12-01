package not.djinni.database.api.application

import not.djinni.database.api.common.SortDirection
import not.djinni.model.application.ApplicationStatusCode

enum class ApplicationSortField {
    CREATED_AT,
    UPDATED_AT
}

data class ApplicationFilter(
    val vacancyId: Long? = null,
    val jobSeekerId: Long? = null,
    val companyId: Long? = null,
    val statusCode: ApplicationStatusCode? = null,
    val sortBy: ApplicationSortField = ApplicationSortField.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC
)
