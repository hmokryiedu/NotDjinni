package not.djinni.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.yaml.YamlConfigLoader
import org.koin.core.annotation.Single
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.Date

@Single(binds = [TokenProvider::class])
class DefaultTokenProvider : TokenProvider {

    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var realm: String
    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey

    override lateinit var configuration: JwtConfiguration
        private set

    override fun initialize() {
        val config = YamlConfigLoader().load("application.yaml")
            ?: error("Failed to load application.yaml")
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
        realm = config.property("jwt.realm").getString()

        privateKey = loadPrivateKey("keys/private_key.pem")
        publicKey = loadPublicKey("keys/public_key.pem")

        configuration = JwtConfiguration(
            issuer = issuer,
            audience = audience,
            realm = realm,
            publicKey = publicKey
        )
    }

    override fun generate(id: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", id)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRES_IN))
            .sign(Algorithm.RSA256(publicKey, privateKey))
    }

    private fun loadPrivateKey(path: String): RSAPrivateKey {
        val keyContent = File(path).readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(keyContent)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun loadPublicKey(path: String): RSAPublicKey {
        val keyContent = File(path).readText()
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(keyContent)
        val keySpec = X509EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    companion object {
        private const val EXPIRES_IN = 3600000L // 1 hour in milliseconds
    }
}