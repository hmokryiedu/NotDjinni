package not.djinni.presentation.router.routes.employer

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.domain.exception.employer.EmployerProfileException
import not.djinni.domain.repository.EmployerProfileRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.employer.mapper.toDomain
import not.djinni.presentation.router.routes.employer.mapper.toResponse
import not.djinni.presentation.router.routes.employer.mapper.toStatusCode
import not.djinni.presentation.router.routes.employer.request.CreateEmployerProfileRequest
import not.djinni.presentation.router.routes.employer.request.UpdateEmployerProfileRequest
import not.djinni.presentation.router.routes.employer.resources.Employer
import org.koin.core.annotation.Single

@Single
class EmployerRoute(
    private val employerProfileRepository: EmployerProfileRepository
) : Route {

    override fun install(root: Routing) = with(root) {
        getProfile()
        createProfile()
        updateProfile()
        deleteProfile()
    }

    private fun Routing.getProfile() {
        authenticate(JwtAuth.NAME) {
            get<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                employerProfileRepository.getProfile(userId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.createProfile() {
        authenticate(JwtAuth.NAME) {
            post<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateEmployerProfileRequest>()
                employerProfileRepository.createProfile(userId, request.toDomain())
                    .onSuccess {
                        call.respond(status = HttpStatusCode.Created, message = it.toResponse())
                    }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.updateProfile() {
        authenticate(JwtAuth.NAME) {
            put<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateEmployerProfileRequest>()
                employerProfileRepository.updateProfile(userId, request.role)
                    .onSuccess { call.respond("Employer profile updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteProfile() {
        authenticate(JwtAuth.NAME) {
            delete<Employer.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                employerProfileRepository.deleteProfile(userId)
                    .onSuccess { call.respond("Employer profile deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = EmployerProfileException::toStatusCode)
            }
        }
    }
}
