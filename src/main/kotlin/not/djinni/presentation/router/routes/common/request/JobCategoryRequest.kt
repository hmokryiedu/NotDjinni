package not.djinni.presentation.router.routes.common.request

import kotlinx.serialization.Serializable

@Serializable
enum class JobCategoryRequest {
    SOFTWARE_DEV,
    DATA_SCIENCE,
    DEVOPS,
    QA,
    PRODUCT_MGMT,
    DESIGN,
    MARKETING,
    SALES,
    HR,
    FINANCE,
    OPERATIONS,
    SUPPORT
}
