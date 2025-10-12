package not.djinni.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.yaml.YamlConfigLoader
import not.djinni.auth.model.JwtConfiguration
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

    private lateinit var privateKey: RSAPrivateKey
    override lateinit var configuration: JwtConfiguration
        private set

    override fun initialize() {
        val config = YamlConfigLoader().load("application.yaml") ?: run {
            error("Failed to load application.yaml")
        }
        configuration = JwtConfiguration(
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            realm = config.property("jwt.realm").getString(),
            publicKey = loadPublicKey()
        )
        privateKey = loadPrivateKey()
    }

    override fun generate(id: String): String {
        return JWT.create()
            .withAudience(configuration.audience)
            .withIssuer(configuration.issuer)
            .withClaim("userId", id)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRES_IN))
            .sign(Algorithm.RSA256(configuration.publicKey, privateKey))
    }

    private fun loadPrivateKey(): RSAPrivateKey {
        val decoded = extractKeyContent("keys/private_key.pem")
        val keySpec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun loadPublicKey(): RSAPublicKey {
        val decoded = extractKeyContent("keys/public_key.pem")
        val keySpec = X509EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    private fun extractKeyContent(path: String): ByteArray {
        return File(path)
            .readText()
            .replace("\\s".toRegex(), "")
            .let(Base64.getDecoder()::decode)
    }

    companion object {
        private const val EXPIRES_IN = 3_600_000L
    }
}