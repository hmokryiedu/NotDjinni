package not.djinni.presentation.router.routes

import io.ktor.server.routing.*

interface Route {
    fun install(root: Routing)
}