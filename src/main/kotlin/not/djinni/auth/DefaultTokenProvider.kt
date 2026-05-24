package not.djinni.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.yaml.*
import not.djinni.auth.model.JwtConfiguration
import not.djinni.presentation.router.routes.common.auth.JwtAuth
import org.koin.core.annotation.Single
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Single(binds = [TokenProvider::class])
class DefaultTokenProvider : TokenProvider {

    private lateinit var privateKey: RSAPrivateKey
    override lateinit var configuration: JwtConfiguration
        private set

    override fun initialize() {
        val config = YamlConfigLoader().load("application.yaml") ?: run {
            error("Failed to load application.yaml")
        }
        val privateKeyPath = config.property("jwt.privateKeyPath").getString()
        val publicKeyPath = config.property("jwt.publicKeyPath").getString()
        configuration = JwtConfiguration(
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            realm = config.property("jwt.realm").getString(),
            privateKeyPath = privateKeyPath,
            publicKeyPath = publicKeyPath,
            publicKey = loadPublicKey(path = publicKeyPath)
        )
        privateKey = loadPrivateKey(path = privateKeyPath)
    }

    override fun generate(id: Long): String {
        return JWT.create()
            .withAudience(configuration.audience)
            .withIssuer(configuration.issuer)
            .withClaim(JwtAuth.USER_ID_CLAIM_NAME, id)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRES_IN))
            .sign(Algorithm.RSA256(configuration.publicKey, privateKey))
    }

    private fun loadPrivateKey(path: String): RSAPrivateKey {
        val decoded = extractKeyContent(path)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun loadPublicKey(path: String): RSAPublicKey {
        val decoded = extractKeyContent(path)
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
