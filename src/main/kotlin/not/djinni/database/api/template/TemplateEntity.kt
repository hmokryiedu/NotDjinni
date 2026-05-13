package not.djinni.database.api.template

data class TemplateEntity(
    val id: Long = 0,
    val seekerId: Long,
    val message: String,
)
