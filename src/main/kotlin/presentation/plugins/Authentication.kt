package not.djinni.presentation.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import not.djinni.auth.TokenProvider
import org.koin.ktor.ext.inject

fun Application.installAuthentication() {
    val tokenProvider by inject<TokenProvider>()
    tokenProvider.initialize()

    val config = tokenProvider.configuration

    install(Authentication) {
        jwt("auth-jwt") {
            realm = config.realm
            verifier(
                JWT
                    .require(Algorithm.RSA256(config.publicKey, null))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("userId").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}