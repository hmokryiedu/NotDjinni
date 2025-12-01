package not.djinni.presentation.router.routes.application.request

import kotlinx.serialization.Serializable

@Serializable
enum class ApplicationStatusRequest {
    APPLIED,
    REVIEWING,
    INTERVIEW,
    TEST_TASK,
    OFFER,
    HIRED,
    REJECTED,
    WITHDRAWN
}
