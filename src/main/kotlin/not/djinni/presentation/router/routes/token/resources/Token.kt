package not.djinni.presentation.router.routes.token.resources

import io.ktor.resources.*

@Resource("/token")
class Token {

    @Resource("validate")
    class Validate(val parent: Token = Token())
}
