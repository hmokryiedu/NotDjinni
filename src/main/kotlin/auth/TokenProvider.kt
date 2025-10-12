package not.djinni.auth

import java.security.interfaces.RSAPublicKey

data class JwtConfiguration(
    val issuer: String,
    val audience: String,
    val realm: String,
    val publicKey: RSAPublicKey
)

interface TokenProvider {
    val configuration: JwtConfiguration

    fun initialize()
    fun generate(id: String): String
}