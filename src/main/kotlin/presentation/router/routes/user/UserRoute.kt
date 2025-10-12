package not.djinni.presentation.router.routes.user

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.get
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.user.resources.User
import org.koin.core.annotation.Single

@Single
class UserRoute : Route {

    override fun install(root: Routing) = with(root) {
        getUser()
    }

    private fun Routing.getUser() {
        authenticate("auth-jwt") {
            get<User> {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                call.respondText("User ID: $userId", status = HttpStatusCode.OK)
            }
        }
    }
}