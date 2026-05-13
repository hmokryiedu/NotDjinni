package not.djinni.presentation.router.routes.seeker.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.routes.common.request.JobCategoryRequest

@Serializable
data class UpdateProfileRequest(
    @SerialName("speciality")
    val specialty: String,
    @SerialName("experience_years")
    val experienceYears: Int,
    @SerialName("desired_salary")
    val desiredSalary: Int,
    @SerialName("about_me")
    val aboutMe: String?,
    @SerialName("job_category")
    val jobCategory: JobCategoryRequest,
)
