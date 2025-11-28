package not.djinni.presentation.router.routes.common.extension

import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.principal
import io.ktor.server.routing.*

val RoutingCall.jwtPayload
    get() = principal<JWTPrincipal>()?.payload

inline fun <reified T : Any> RoutingContext.getClaim(name: String): T? {
    val claim = call.jwtPayload?.getClaim(name) ?: return null
    return with(claim) {
        when (T::class) {
            String::class -> asString()
            Long::class -> asLong()
            Int::class -> asInt()
            Boolean::class -> asBoolean()
            Double::class -> asDouble()
            else -> this.`as`(T::class.java)
        } as? T?
    }
}