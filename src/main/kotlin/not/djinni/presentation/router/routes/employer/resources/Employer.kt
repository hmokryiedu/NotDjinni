package not.djinni.presentation.router.routes.employer.resources

import io.ktor.resources.*

@Resource("/employer")
class Employer {
    @Resource("profile")
    class Profile(val parent: Employer = Employer())
}
