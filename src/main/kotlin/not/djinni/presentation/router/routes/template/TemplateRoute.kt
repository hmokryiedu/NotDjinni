package not.djinni.presentation.router.routes.template

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import not.djinni.domain.exception.template.TemplateException
import not.djinni.domain.repository.TemplateRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.seeker.resources.Seeker
import not.djinni.presentation.router.routes.template.mapper.toResponse
import not.djinni.presentation.router.routes.template.mapper.toStatusCode
import not.djinni.presentation.router.routes.template.request.TemplateRequest
import org.koin.core.annotation.Single

@Single
class TemplateRoute(
    private val templateRepository: TemplateRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        createTemplate()
        getTemplates()
        getTemplate()
        updateTemplate()
        deleteTemplate()
    }

    private fun Routing.createTemplate() {
        authenticate(JwtAuth.NAME) {
            post<Seeker.Templates> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<TemplateRequest>()
                templateRepository.createTemplate(userId = userId, message = request.message)
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = it.toResponse()) }
                    .handleError(call = call, mapToCode = TemplateException::toStatusCode)
            }
        }
    }

    private fun Routing.getTemplates() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.Templates> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                templateRepository.getTemplates(userId = userId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = TemplateException::toStatusCode)
            }
        }
    }

    private fun Routing.getTemplate() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.TemplateById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                templateRepository.getTemplate(userId = userId, id = resource.id)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = TemplateException::toStatusCode)
            }
        }
    }

    private fun Routing.updateTemplate() {
        authenticate(JwtAuth.NAME) {
            put<Seeker.TemplateById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<TemplateRequest>()
                templateRepository.updateTemplate(userId = userId, id = resource.id, message = request.message)
                    .onSuccess { call.respond("Template updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = TemplateException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteTemplate() {
        authenticate(JwtAuth.NAME) {
            delete<Seeker.TemplateById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                templateRepository.deleteTemplate(userId = userId, id = resource.id)
                    .onSuccess { call.respond("Template deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = TemplateException::toStatusCode)
            }
        }
    }
}
