package not.djinni.domain.exception.template

sealed class TemplateException(override val message: String) : Throwable() {
    class SeekerProfileNotFound : TemplateException("Seeker profile does not exist")
    class InvalidTemplateData : TemplateException("Template message must not be blank")
    class TemplateNotFound : TemplateException("Template not found")
}
