package not.djinni.presentation.router.routes.seeker.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeekerProfileResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("about_me")
    val aboutMe: String?,
    @SerialName("speciality")
    val specialty: String,
    @SerialName("desired_salary")
    val desiredSalary: Int,
    @SerialName("experience_years")
    val experienceYears: Int,
    @SerialName("work_experience")
    val workExperience: List<WorkExperienceResponse>
)
