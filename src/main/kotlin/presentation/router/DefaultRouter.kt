package not.djinni.presentation.router

import io.ktor.server.application.*
import io.ktor.server.routing.routing
import not.djinni.presentation.router.routes.auth.AuthRoute
import org.koin.core.annotation.Single

@Single([Router::class])
class DefaultRouter(
    private val authRoute: AuthRoute
) : Router {

    override fun install(application: Application) {
        application.routing {
            authRoute.install(this)
        }
    }
}