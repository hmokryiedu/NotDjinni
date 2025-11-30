package not.djinni.database.api.employer

data class CompanyEntity(
    val id: Long = 0,
    val companyName: String,
    val website: String?,
    val description: String
)
