package not.djinni.model.role

data class SeekerProfile(
    val id: Long,
    val aboutMe: String?,
    val speciality: String,
    val desiredSalary: Int,
    val experienceYears: Int,
    val workExperience: List<WorkExperience> = emptyList()
)
