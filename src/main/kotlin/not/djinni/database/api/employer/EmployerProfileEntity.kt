package not.djinni.database.api.employer

data class EmployerProfileEntity(
    val id: Long = 0,
    val userId: Long,
    val companyId: Long,
    val role: String
)
