package not.djinni.data.mapper

import not.djinni.database.api.template.TemplateEntity
import not.djinni.model.template.Template

fun TemplateEntity.toDomain() = Template(
    id = id,
    message = message,
)
