package not.djinni.presentation.router

import io.ktor.server.application.*
import io.ktor.server.routing.routing
import not.djinni.presentation.router.routes.HelloWorldRoute
import org.koin.core.annotation.Single

@Single([Router::class])
class DefaultRouter(
    private val helloWorldRoute: HelloWorldRoute,
) : Router {

    override fun install(application: Application) {
        application.routing {
            helloWorldRoute.install(this)
        }
    }
}