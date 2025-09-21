package not.djinni.presentation.router.routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single

@Single
class HelloWorldRoute : Route {

    override fun install(root: Routing) = with(root) {
        helloWorldRoute()
    }

    private fun Routing.helloWorldRoute() {
        get("/hello") {
            call.respondText("Hello, world!")
        }
    }
}