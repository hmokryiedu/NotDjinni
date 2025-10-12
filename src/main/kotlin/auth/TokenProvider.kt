package not.djinni.auth

import not.djinni.auth.model.JwtConfiguration

interface TokenProvider {
    val configuration: JwtConfiguration

    fun initialize()
    fun generate(id: String): String
}