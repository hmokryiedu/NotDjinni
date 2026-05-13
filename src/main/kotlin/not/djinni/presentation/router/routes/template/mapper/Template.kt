package not.djinni.presentation.router.routes.template.mapper

import not.djinni.model.template.Template
import not.djinni.presentation.router.routes.template.response.TemplateListResponse
import not.djinni.presentation.router.routes.template.response.TemplateResponse

fun Template.toResponse() = TemplateResponse(
    id = id,
    message = message,
)

fun List<Template>.toResponse() = TemplateListResponse(
    templates = map { it.toResponse() },
)
