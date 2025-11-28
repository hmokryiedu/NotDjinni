package not.djinni.model.token

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)