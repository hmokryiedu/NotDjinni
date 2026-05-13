package not.djinni.presentation.router.routes.template.mapper

import io.ktor.http.HttpStatusCode
import not.djinni.domain.exception.template.TemplateException

fun TemplateException.toStatusCode(): HttpStatusCode = when (this) {
    is TemplateException.SeekerProfileNotFound -> HttpStatusCode.NotFound
    is TemplateException.InvalidTemplateData -> HttpStatusCode.BadRequest
    is TemplateException.TemplateNotFound -> HttpStatusCode.NotFound
}
