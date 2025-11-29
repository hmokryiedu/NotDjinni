package not.djinni.presentation.router.routes.seeker

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import not.djinni.domain.exception.seeker.SeekerProfileException
import not.djinni.domain.repository.SeekerProfileRepository
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.extension.handleError
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.common.extension.getUserIdFromTokenOrSendError
import not.djinni.presentation.router.routes.seeker.mapper.toDomain
import not.djinni.presentation.router.routes.seeker.mapper.toResponse
import not.djinni.presentation.router.routes.seeker.mapper.toStatusCode
import not.djinni.presentation.router.routes.seeker.request.CreateProfileRequest
import not.djinni.presentation.router.routes.seeker.request.UpdateProfileRequest
import not.djinni.presentation.router.routes.seeker.request.WorkExperienceRequest
import not.djinni.presentation.router.routes.seeker.resources.Seeker
import not.djinni.presentation.router.routes.seeker.response.WorkExperienceIdResponse
import org.koin.core.annotation.Single

@Single
class SeekerRoute(
    private val seekerProfileRepository: SeekerProfileRepository
) : Route {

    override fun install(root: Routing) = with(root) {
        getProfile()
        createProfile()
        updateProfile()
        deleteProfile()
        addWorkExperience()
        updateWorkExperience()
        deleteWorkExperience()
    }

    private fun Routing.getProfile() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                seekerProfileRepository.getProfile(userId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.createProfile() {
        authenticate(JwtAuth.NAME) {
            post<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<CreateProfileRequest>()
                seekerProfileRepository.createProfile(userId, request.toDomain())
                    .onSuccess { call.respond(status = HttpStatusCode.Created, message = it.toResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.updateProfile() {
        authenticate(JwtAuth.NAME) {
            put<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<UpdateProfileRequest>()
                seekerProfileRepository.updateProfile(userId, request.toDomain())
                    .onSuccess { call.respond("Profile updated successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteProfile() {
        authenticate(JwtAuth.NAME) {
            delete<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                seekerProfileRepository.deleteProfile(userId)
                    .onSuccess { call.respond("Profile deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.addWorkExperience() {
        authenticate(JwtAuth.NAME) {
            post<Seeker.Experience> {
                val userId = getUserIdFromTokenOrSendError() ?: return@post
                val request = call.receive<WorkExperienceRequest>()
                seekerProfileRepository.addWorkExperience(userId, request.toDomain())
                    .onSuccess { experienceId ->
                        call.respond(status = HttpStatusCode.Created, message = WorkExperienceIdResponse(experienceId))
                    }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.updateWorkExperience() {
        authenticate(JwtAuth.NAME) {
            put<Seeker.ExperienceById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@put
                val request = call.receive<WorkExperienceRequest>()
                seekerProfileRepository.updateWorkExperience(
                    userId = userId,
                    experienceId = resource.id,
                    experience = request.toDomain()
                ).onSuccess {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = mapOf("message" to "Work experience updated successfully")
                    )
                }.handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }

    private fun Routing.deleteWorkExperience() {
        authenticate(JwtAuth.NAME) {
            delete<Seeker.ExperienceById> { resource ->
                val userId = getUserIdFromTokenOrSendError() ?: return@delete
                seekerProfileRepository.deleteWorkExperience(userId, resource.id)
                    .onSuccess { call.respond("Work experience deleted successfully".toMessageResponse()) }
                    .handleError(call = call, mapToCode = SeekerProfileException::toStatusCode)
            }
        }
    }
}
