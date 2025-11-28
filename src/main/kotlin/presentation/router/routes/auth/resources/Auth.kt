package not.djinni.presentation.router.routes.auth.resources

import io.ktor.resources.*

@Resource("/auth")
class Auth {
    @Resource("login")
    class Login(val parent: Auth = Auth())

    @Resource("register")
    class Register(val parent: Auth = Auth())

    @Resource("refresh")
    class Refresh(val parent: Auth = Auth())

    @Resource("logout")
    class Logout(val parent: Auth = Auth())

    @Resource("logout-all")
    class LogoutAll(val parent: Auth = Auth())
}
