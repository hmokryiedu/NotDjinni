package not.djinni.presentation.router.routes.health

import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.health.resources.Health
import not.djinni.presentation.router.routes.health.response.HealthResponse
import org.koin.core.annotation.Single

@Single
class HealthRoute : Route {

    override fun install(root: Routing) {
        with(root) {
            get<Health> {
                call.respond(HttpStatusCode.OK, HealthResponse(status = "ok"))
            }
        }
    }
}
