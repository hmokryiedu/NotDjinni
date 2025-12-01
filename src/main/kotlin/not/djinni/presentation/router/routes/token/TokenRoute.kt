package not.djinni.presentation.router.routes.token

import io.ktor.http.*
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import not.djinni.presentation.router.common.response.common.toMessageResponse
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import not.djinni.presentation.router.routes.token.resources.Token
import org.koin.core.annotation.Single

@Single
class TokenRoute : Route {

    override fun install(root: Routing) = with(root) {
        validate()
    }

    private fun Routing.validate() {
        authenticate(JwtAuth.NAME) {
            get<Token.Validate> {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = "Token is valid".toMessageResponse()
                )
            }
        }
    }
}