package not.djinni.database.api.seeker

data class SeekerProfileEntity(
    val id: Long = 0,
    val userId: Long,
    val specialty: String,
    val experienceYears: Int,
    val desiredSalary: Int,
    val aboutMe: String?
)
