package not.djinni.auth.model

import java.security.interfaces.RSAPublicKey

data class JwtConfiguration(
    val issuer: String,
    val audience: String,
    val realm: String,
    val publicKey: RSAPublicKey
)